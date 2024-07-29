package com.noesis.telco.summary.manager.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.noesis.domain.persistence.NgConnectSummeryAll;
import com.noesis.domain.persistence.NgUser;
import com.noesis.domain.repository.NgConnectSummeryAllRepository;
import com.noesis.domain.service.UserService;

@Service
public class NgMisLogSummaryService {

//	@Autowired
//	private NgConnectSummeryAllRepository ngConnectSummeryAllRepository;
//
//	@Autowired
//	@Qualifier("redisTelcoForSummary")
//	@Resource(name = "redisTelcoForSummary")
//	private RedisTemplate<String, Integer> redisTelcoForSummary;
//
//	@Autowired
//	private UserService userService;
//
//	private static final Logger logger = LoggerFactory.getLogger(NgMisLogSummaryService.class);
//
//	private List<NgConnectSummeryAll> updateConnectDataForCurrentDateFromRedisToDB(Date currentDate, String username,
//			String date) {
//		NgUser ngUser = userService.getUserByNameFromDb(username);
//		logger.info("Getting telco summary for date {} and username {} ", date, username);
//
//		String submittedKey = date + ":connectsubmitted:" + username;
//		String deliveredKey = date + ":connectdelivered:" + username;
//		String failedKey = date + ":connectfailed:" + username;
//
//		Map<Object, Object> submittedMap = (Map<Object, Object>) redisTelcoForSummary.opsForHash()
//				.entries(submittedKey);
//		Map<Object, Object> deliveredMap = (Map<Object, Object>) redisTelcoForSummary.opsForHash()
//				.entries(deliveredKey);
//		Map<Object, Object> failedMap = (Map<Object, Object>) redisTelcoForSummary.opsForHash().entries(failedKey);
//
//		Set<Object> uniqueSmscIdKeys = submittedMap.keySet();
//		logger.info("SMSC id key set : {} ", uniqueSmscIdKeys);
//		List<NgConnectSummeryAll> ngConnectListToBeUpdated = new ArrayList<>();
//		List<NgConnectSummeryAll> existingConnectObjectsList = ngConnectSummeryAllRepository
//				.findByUserNameAndDate(username, currentDate);
//
//		for (Object connectIdObject : uniqueSmscIdKeys) {
//			String submitted = "0";
//			String delivered = "0";
//			String failed = "0";
//			String awaited = "0";
//
//			String smscIdString = (String) connectIdObject;
//			logger.info("Going to update connect summary for smsc id {} and date {} : ", smscIdString, date);
//
//			if (submittedMap.containsKey(smscIdString)) {
//				submitted = (String) submittedMap.get(smscIdString);
//			}
//			if (deliveredMap.containsKey(smscIdString)) {
//				delivered = (String) deliveredMap.get(smscIdString);
//			}
//			if (failedMap.containsKey(smscIdString)) {
//				failed = (String) failedMap.get(smscIdString);
//			}
//
//			awaited = "" + (Integer.parseInt(submitted) - (Integer.parseInt(delivered) + Integer.parseInt(failed)));
//
//			NgConnectSummeryAll existingConnectReportObject = existingConnectObjectsList.stream()
//					.filter(c -> c.getSmscId().equals(smscIdString)).findAny().orElse(null);
//
//			if (existingConnectReportObject != null) {
//				logger.info("Found Telco Report Object in DB for date {}, username {}, campaign id {}", date, username,
//						smscIdString);
//				existingConnectReportObject.setSubmitCnt(submitted);
//				existingConnectReportObject.setDlvrdCnt(delivered);
//				existingConnectReportObject.setFailCnt(failed);
//				existingConnectReportObject.setAwaited(awaited);
//				ngConnectListToBeUpdated.add(existingConnectReportObject);
//			} else {
//				logger.info("Creating connect Report Object in DB for date {}, username {}, senderid {}", date,
//						username, smscIdString);
//				NgConnectSummeryAll connectNewSummeryAll = new NgConnectSummeryAll();
//				connectNewSummeryAll.setSubmitCnt(submitted);
//				connectNewSummeryAll.setDlvrdCnt(delivered);
//				connectNewSummeryAll.setFailCnt(failed);
//				connectNewSummeryAll.setAwaited(awaited);
//				connectNewSummeryAll.setUserName(username);
//				connectNewSummeryAll.setDate(currentDate);
//				connectNewSummeryAll.setUserId(ngUser.getId());
//				connectNewSummeryAll.setAdId(ngUser.getAdId());
//				connectNewSummeryAll.setPaId(ngUser.getParentId());
//				connectNewSummeryAll.setSeId(ngUser.getSeId());
//				connectNewSummeryAll.setReId(ngUser.getReId());
//				connectNewSummeryAll.setSaId(ngUser.getSaId());
//				connectNewSummeryAll.setSmscId(smscIdString);
//				connectNewSummeryAll.setKannelId(0);   			//
//				connectNewSummeryAll.setSenderId(null);   //
//				connectNewSummeryAll.setCarrierId(0);
//				connectNewSummeryAll.setCircleId(0);
//				ngConnectListToBeUpdated.add(connectNewSummeryAll);
//			}
//
//		}
//
//		logger.info("Going to save total {} campaign objects for user {} and date {}", ngConnectListToBeUpdated.size(),
//				date, username);
//		// ngMisLogSummaryRepository.save(ngSummaryListToBeUpdated);
//		logger.info("Telco Summary updated successfully for current date.");
//
//		return ngConnectListToBeUpdated;
//	}
//
//	public void updateAllUsersCampaignsForCurrentDateFromRedisToDB() {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//		LocalDateTime currentDateTime = LocalDateTime.now();
//		String currentDate = dtf.format(currentDateTime);
//		Date currDate;
//		
//		List<NgConnectSummeryAll> finalListToBeUpdated = new LinkedList<NgConnectSummeryAll>();
//		try {
//			currDate = sdf.parse(currentDate);
//
//			// update summary data for current date.
//			Iterable<NgUser> usersList = userService.getAllUserList();
//			for (NgUser ngUser : usersList) {
//				try{
//					logger.info("Going to update summary for user {} for date {}", ngUser.getUserName(), currentDate);
//					List<NgConnectSummeryAll> listToBeUpdated = updateConnectDataForCurrentDateFromRedisToDB(currDate,
//							ngUser.getUserName(), currentDate);
//					finalListToBeUpdated.addAll(listToBeUpdated);
//				} catch (Exception e) {
//					logger.error("Error while update user {} summary data for date {}", ngUser.getUserName(),
//							currentDate);
//				}
//			}
//			if (finalListToBeUpdated.size() > 0) {
//				ngConnectSummeryAllRepository.save(finalListToBeUpdated);
//			}
//		} catch (ParseException e) {
//			logger.error("Error while updating all users campaign summary data in db.");
//			e.printStackTrace();
//		}
//	}

}
