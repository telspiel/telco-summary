package com.noesis.telco.summary.manager.reader;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noesis.domain.persistence.NgMisMessage;
import com.noesis.domain.persistence.NgUser;
import com.noesis.domain.service.UserService;


public class MisMessageReader {

	private static final Logger logger = LoggerFactory.getLogger(MisMessageReader.class);

	private int maxPollRecordSize;

	private CountDownLatch latch = new CountDownLatch(maxPollRecordSize);

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserService userService;

	@Value("${app.name}")
	private String appName;

	@Value("${kafka.misreader.sleep.interval.ms}")
	private String misReaderSleepInterval;

	@Autowired
	@Qualifier("redisTelcoForSummary")
	private RedisTemplate<String, Integer> redisTelcoForSummary;

	@Value("${mis.summary.expiry.seconds}")
	private String misSummaryExpirySeconds;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public MisMessageReader(@Value("${kafka.max.poll.records.size}") int maxPollRecordSize) {
		this.maxPollRecordSize = maxPollRecordSize;
		this.latch = new CountDownLatch(maxPollRecordSize);
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	@KafkaListener(id = "mis-" + "${app.name}", topics = "${kafka.topic.name.mis.object}")
	public void receive(List<String> messageList, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) List<Integer> partitions,
			@Header(KafkaHeaders.OFFSET) List<Long> offsets) {

		logger.info("Start of Telco Message batch read of size: "+messageList.size());
		for (int i = 0; i < messageList.size(); i++) {
			logger.info("Received telco message='{}' with partition-offset='{}'", messageList.get(i),
					partitions.get(i) + "-" + offsets.get(i));
			try{
				NgMisMessage ngMisMessage = convertReceivedJsonMessageIntoTelcoObject(messageList.get(i));
					if (ngMisMessage != null) {
						String dateKey = sdf.format(ngMisMessage.getReceivedTs());
						NgUser user = userService.getUserById(ngMisMessage.getUserId());
						logger.info("Telco message received for user is : {}", user.getUserName());

						if(ngMisMessage.getStatus().equalsIgnoreCase("SUBMITTED") && (ngMisMessage.getFailedRetryCount() == null || ngMisMessage.getFailedRetryCount() ==0 )){
							String dateSubmittedKey = dateKey+":connectsubmitted:"+user.getUserName();
							logger.info("increasing summary count for key : {} and user {}",dateSubmittedKey, user.getUserName());
							redisTelcoForSummary.opsForHash().increment(dateSubmittedKey, ngMisMessage.getKannelName().toString(), 1);
							if(redisTelcoForSummary.getExpire(dateSubmittedKey) == null || redisTelcoForSummary.getExpire(dateSubmittedKey) == -1){
								redisTelcoForSummary.expire(dateSubmittedKey, Long.parseLong(misSummaryExpirySeconds), TimeUnit.SECONDS);
							}
						}

					}	
				}catch (Exception e){
					logger.error("Exception occured while processing MIS message. Hence skipping this message: {} "+messageList.get(i));
					e.printStackTrace();
				}
			latch.countDown();
		}
		logger.info("End of received Telco MIS batch.");
	    try {
	    	logger.info("Telco MIS Reader Thread Going To Sleep for "+misReaderSleepInterval + "ms.");
	    	Thread.sleep(Integer.parseInt(misReaderSleepInterval));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
	}



	private NgMisMessage convertReceivedJsonMessageIntoTelcoObject(String misMessageObjectJsonString) {
		NgMisMessage ngMisMessageObject = null;
		try {
			ngMisMessageObject = objectMapper.readValue(misMessageObjectJsonString, NgMisMessage.class);
		} catch (Exception e) {
			logger.error("Dont retry this message as error while parsing MIS Message json string: "
					+ misMessageObjectJsonString);
			e.printStackTrace();
		}
		return ngMisMessageObject;
	}

}
