package kr.co.inslab.codealley.filemanager.backup.controller;

import kr.co.inslab.codealley.filemanager.backup.service.RestoreService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/restore")
public class RestoreController {
	
private Log log = LogFactory.getLog(RestoreController.class);
	@Autowired
	private RestoreService restoreService;

	/**
	 * 복구
	 * @param groupName
	 * @return
	 */
	@RequestMapping("/{groupName}")
	@ResponseBody
	public ResponseEntity<String> restore(@PathVariable("groupName") String groupName){
		try {
			restoreService.restore(groupName, null);
		} catch (Exception e) {
			log.error(e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.toString());
		}
		
		return ResponseEntity.ok("completed");
	}
	
	/**
	 * 복구(특정 tools 선택)
	 * @param groupName
	 * @param toolName
	 * @return
	 */
	@RequestMapping("/{groupName}/{toolName}")
	@ResponseBody
	public ResponseEntity<String> restoreWithTools(@PathVariable("groupName") String groupName, @PathVariable("toolName") String toolName ){
		try {
			restoreService.restore(groupName, toolName);
		} catch (Exception e) {
			log.error(e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.toString());
		}
		
		return ResponseEntity.ok("completed");
	}
	
	
	
}
