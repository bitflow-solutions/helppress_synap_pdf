package ai.bitflow.helppress.publisher.controller;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.bitflow.helppress.publisher.domain.Contents;
import ai.bitflow.helppress.publisher.service.ContentsService;
import ai.bitflow.helppress.publisher.service.NodeService;
import ai.bitflow.helppress.publisher.util.SpringUtil;
import ai.bitflow.helppress.publisher.vo.req.ContentsReq;
import ai.bitflow.helppress.publisher.vo.req.UpdateNodeReq;
import ai.bitflow.helppress.publisher.vo.res.ContentsRes;
import ai.bitflow.helppress.publisher.vo.res.result.ContentResult;
import ai.bitflow.helppress.publisher.vo.res.result.NodeUpdateResult;

/**
 * 
 * @author method76
 */
@RestController
@RequestMapping("/api/v1/ecm/content") 
public class ApiContentController {
	
	private final Logger logger = LoggerFactory.getLogger(ApiContentController.class);
	
	@Autowired
	private ContentsService cservice;

	@Autowired
	private NodeService nservice;
	
	@Autowired 
	private SimpMessagingTemplate broker;
	
	/**
	 * HTML 도움말 DB에서 가져오기
	 * @param id
	 * @return
	 */
	@GetMapping("/{groupid}/{id}")
	public ContentsRes get(@PathVariable String groupid, @PathVariable String id) {
		ContentsRes ret = new ContentsRes();
		Contents item = cservice.getContent(groupid, id);
		ContentResult result = new ContentResult();
		if (item!=null) {
			result.setTitle(item.getTitle());
			result.setContents(item.getContent());
		} else {
			ret.setFailResponse(404);
		}
		ret.setResult(result);
		return ret;
	}
	
	/**
	 * 컨텐츠 수정
	 * (HTML의 경우 DB 업데이트, PDF의 경우 파일 업로드)
	 * @param params
	 * @param id
	 * @return
	 */
	@PutMapping("/{groupid}")
	public ContentsRes updateContent(ContentsReq params, @PathVariable String groupid, HttpSession sess) {
		logger.debug("params " + params.toString());
		ContentsRes ret1 = new ContentsRes();
		ContentResult result = new ContentResult();
		String username = SpringUtil.getSessionUserid(sess);
		if (username==null) {
			ret1.setFailResponse(401);
		} else {
			String res1 = null;
			if (params.getFile1()==null) {
				// 에디터로 HTML 수정한 경우
				res1 = cservice.updateContent(params, groupid, username);
			} else {
				// PDF파일 업로드 한 경우
				res1 = cservice.updatePdfContent(params, groupid, username);
			}

			if (res1==null) {
				ret1.setError("도움말을 수정하지 못했습니다");
			} else if (params.getMenuCode()!=null && params.getMenuCode().length()>0) {
				
				UpdateNodeReq params2 = new UpdateNodeReq();
				params2.setGroupId(groupid);
				params2.setKey(params.getKey());
				params2.setMenuCode(params.getMenuCode());
				NodeUpdateResult res2 = nservice.updateNode(params2, username);
				res2.setUsername(username);
				broker.convertAndSend("/group", res2);
				
				logger.debug("ws msg sent");
			}
		}
		result.setKey(params.getMenuCode());
		ret1.setResult(result);
		return ret1;
	}

}
