package ai.bitflow.helppress.publisher.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ai.bitflow.helppress.publisher.service.ReleaseService;
import ai.bitflow.helppress.publisher.util.SpringUtil;

/**
 * 배포를 위한 다운로드
 * @author method76
 */
@RestController
@RequestMapping("/api/v1/ecm/release") 
public class ApiReleaseController {
	
	private final Logger logger = LoggerFactory.getLogger(ApiReleaseController.class);
	
	@Autowired
	private ReleaseService rservice;

	@Autowired 
	private SimpMessagingTemplate broker;
	
	/**
	 * 전체 파일 ZIP 다운로드
	 * @param params
	 * @return
	 */
	@GetMapping("/all") 
	public void downloadAll(@RequestParam Boolean release, HttpServletResponse res, HttpSession sess) {
		logger.debug("downloadAll " + release);
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null || username.length()<1) {
			try {
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			rservice.downloadAll(release, res, username);
			broker.convertAndSend("/download", release);
		}
	}
	
	/**
	 * 1개 도움말 ZIP 파일 다운로드
	 * @param key
	 * @param res
	 */
	@GetMapping("/{groupId}/{contentId}") 
	public void downloadOne(@PathVariable String groupId, @PathVariable String contentId, HttpServletResponse res) {
		logger.debug("downloadOne");
		rservice.downloadOne(groupId, contentId, res);
	}
	
	/**
	 * 
	 * @param key
	 * @param res
	 */
	@GetMapping("/all/{id}") 
	public void downloadFromHistory(@PathVariable Integer id, HttpServletResponse res) {
		logger.debug("downloadFromHistory");
		rservice.downloadFromHistory(id, res);
	}
	
	/**
	 * 
	 * @param res
	 */
	@GetMapping("/changed") 
	public void downloadChanged(@RequestParam String fileIds, @RequestParam Boolean release, HttpServletResponse res, HttpSession sess) {
		logger.debug("downloadChanged");
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null || username.length()<1) {
			try {
				res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String[] fileIdsArr = fileIds.split(",");
			List<String> idList =  new ArrayList<>();
			for (String path : fileIdsArr) {
				idList.add(path);
			}
			if (idList!=null && idList.size()>0) {
				rservice.downloadChanged(idList, res, username, release);
			}
			broker.convertAndSend("/download", release);
		}
		
	}
	
}
