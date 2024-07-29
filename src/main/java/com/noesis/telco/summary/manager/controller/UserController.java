package com.noesis.telco.summary.manager.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.noesis.domain.service.UserService;
import com.noesis.telco.summary.manager.service.NgMisLogSummaryService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@RestController
@CrossOrigin
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	UserService userService;
	
	@Autowired
	NgMisLogSummaryService telcoSummeryAllService;
	
	@Autowired
	@Qualifier("redisTelcoForSummary")
	private RedisTemplate<String, Integer> redisTelcoForSummary;

	
	@RequestMapping(value = "/getUserConnectSummaryForDate", method = RequestMethod.GET)
	public String getConnect() {
	    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	    logger.info("Getting summary for the current date {}.", date);

	    // Map to aggregate counts by smscId
	    Map<String, SummaryMisFormResponseDataGrid> aggregatedData = new HashMap<>();

	    String submittedPattern = date + ":connectsubmitted:";
	    String deliveredPattern = date + ":connectdelivered:";
	    String failedPattern = date + ":connectfailed:";

	    logger.info("Current key patterns: submitted={}, delivered={}, failed={}", submittedPattern, deliveredPattern, failedPattern);

	    // Fetch all keys matching the submitted pattern
	    Set<String> submittedKeys = redisTelcoForSummary.keys(submittedPattern + "*");
	    logger.info("Submitted keys found: {}", submittedKeys);

	    for (String key : submittedKeys) {
	        String username = key.substring(submittedPattern.length());
	        String submittedKey = submittedPattern + username;
	        String deliveredKey = deliveredPattern + username;
	        String failedKey = failedPattern + username;

	        logger.info("Processing user: {}", username);

	        // Directly retrieve and print data
	        Map<Object, Object> submittedMap = (Map<Object, Object>) redisTelcoForSummary.opsForHash().entries(submittedKey);
	        Map<Object, Object> deliveredMap = (Map<Object, Object>) redisTelcoForSummary.opsForHash().entries(deliveredKey);
	        Map<Object, Object> failedMap = (Map<Object, Object>) redisTelcoForSummary.opsForHash().entries(failedKey);
	        Set<Object> uniqueSmscIdKeys = submittedMap.keySet();

	        for (Object smscIdObject : uniqueSmscIdKeys) {
	            String smscIdString = (String) smscIdObject;

	            int submitted = submittedMap.containsKey(smscIdString) ? Integer.parseInt((String) submittedMap.get(smscIdString)) : 0;
	            int delivered = deliveredMap.containsKey(smscIdString) ? Integer.parseInt((String) deliveredMap.get(smscIdString)) : 0;
	            int failed = failedMap.containsKey(smscIdString) ? Integer.parseInt((String) failedMap.get(smscIdString)) : 0;

	            if (aggregatedData.containsKey(smscIdString)) {
	                // Update existing entry
	                SummaryMisFormResponseDataGrid data = aggregatedData.get(smscIdString);
	                data.setTotalSubmit(String.valueOf(Integer.parseInt(data.getTotalSubmit()) + submitted));
	                data.setTotalDelivered(String.valueOf(Integer.parseInt(data.getTotalDelivered()) + delivered));
	                data.setTotalFailed(String.valueOf(Integer.parseInt(data.getTotalFailed()) + failed));
	                data.setTotalAwaited(String.valueOf(Integer.parseInt(data.getTotalSubmit()) - (Integer.parseInt(data.getTotalDelivered()) + Integer.parseInt(data.getTotalFailed()))));
	            } else {
	                // Create new entry
	                SummaryMisFormResponseDataGrid data = new SummaryMisFormResponseDataGrid();
	                data.setSummaryDate(date);
	                data.setSmscId(smscIdString);
	                data.setTotalSubmit(String.valueOf(submitted));
	                data.setTotalDelivered(String.valueOf(delivered));
	                data.setTotalFailed(String.valueOf(failed));
	                data.setTotalAwaited(String.valueOf(submitted - (delivered + failed)));
	                aggregatedData.put(smscIdString, data);
	            }
	        }
	    }

	    List<SummaryMisFormResponseDataGrid> gridData = new ArrayList<>(aggregatedData.values());
	    logger.info("Connect Data: {}", gridData);
	    return gridData.toString();
	}

	
//	@RequestMapping(value = "/updateAllUsersConnectSummaryData", method = RequestMethod.GET)
//	public String updateAllUsersSummaryData() {
//		telcoSummeryAllService.updateAllUsersCampaignsForCurrentDateFromRedisToDB();
//
//		return "Telco summary updated successfully....";
//	}
	
	
}


class SummaryMisFormResponseDataGrid {
	
	private String summaryDate;
	private String totalSubmit;
	private String totalDelivered;
	private String totalFailed;
	private String totalAwaited;
	private String smscId;
	
	
	public String getSummaryDate() {
		return summaryDate;
	}
	public void setSummaryDate(String summaryDate) {
		this.summaryDate = summaryDate;
	}
	
	public String getTotalSubmit() {
		return totalSubmit;
	}
	public void setTotalSubmit(String totalSubmit) {
		this.totalSubmit = totalSubmit;
	}
	public String getTotalDelivered() {
		return totalDelivered;
	}
	public void setTotalDelivered(String totalDelivered) {
		this.totalDelivered = totalDelivered;
	}
	public String getTotalFailed() {
		return totalFailed;
	}
	public void setTotalFailed(String totalFailed) {
		this.totalFailed = totalFailed;
	}
	
	public String getTotalAwaited() {
		return totalAwaited;
	}
	public void setTotalAwaited(String totalAwaited) {
		this.totalAwaited = totalAwaited;
	}
	
	public String getSmscId() {
		return smscId;
	}
	public void setSmscId(String smscId) {
		this.smscId = smscId;
	}
	@Override
	public String toString() {
		return "SummaryMisFormResponseDataGrid [summaryDate=" + summaryDate + ", totalSubmit=" + totalSubmit
				+ ", totalDelivered=" + totalDelivered + ", totalFailed=" + totalFailed + ", totalAwaited="
				+ totalAwaited + ", smscId=" + smscId + "]";
	}
	
}
