package kr.co.inslab.codealley.filemanager.app.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inslab.codealley.filemanager.app.model.AppConfigFile;
import kr.co.inslab.codealley.filemanager.app.service.FileService;
import kr.co.inslab.codealley.filemanager.app.utils.Response;

@RequestMapping("/api")
@Controller
public class MainController {

	private Log logger = LogFactory.getLog(MainController.class);

	@Autowired
	private FileService fileService;

	/**
	 * App config file 요청
	 * 
	 * @param groupId
	 * @param appId
	 * @param name
	 * @return
	 */
	@GetMapping(value = "/app/config/{groupId}/{appId}/{name}")
	@ResponseBody
	public String getAppConfigFile(@PathVariable("groupId") String groupId, @PathVariable("appId") String appId,
			@PathVariable("name") String name) {
		logger.info("App config file requested.");

		String dataPath = String.format("/opt/data/%s/%s/%s/conf", groupId, appId, name);
		List<AppConfigFile> files = fileService.getAppConfigFile(dataPath);

		if (files.isEmpty()) {
			return new Response(false).toString();
		}

		return new Response("files", files.toString()).toString();
	}

	/**
	 * App config file 다운로드
	 * 
	 * @param response
	 * @param groupId
	 * @param appId
	 * @param name
	 * @param value
	 * @return
	 */
	@GetMapping(value = "/app/config/download/{groupId}/{appId}/{name}/{fileName:.+}")
	@ResponseBody
	public String downloadAppConfigFile(HttpServletResponse response, @PathVariable("groupId") String groupId,
			@PathVariable("appId") String appId, @PathVariable("name") String name,
			@PathVariable("fileName") String fileName) {
		logger.info("App config file download requested.");

		String dataPath = String.format("/opt/data/%s/%s/%s/conf", groupId, appId, name);
		List<AppConfigFile> files = fileService.getAppConfigFile(dataPath);
		boolean downloaded = false;

		if (files.isEmpty()) {
			return new Response(downloaded).toString();
		}
		downloaded = fileService.downloadAppConfigFile(response, dataPath, fileName);

		return new Response(downloaded).toString();
	}

	/**
	 * App config file 업로드
	 * 
	 * @param request
	 * @param response
	 * @param groupId
	 * @param appId
	 * @param name
	 * @return
	 */
	@PostMapping(value = "/app/config/upload/{groupId}/{appId}/{name}")
	@ResponseBody
	public String uploadAppConfigFile(@PathVariable("groupId") String groupId, @PathVariable("appId") String appId,
			@PathVariable("name") String name, @RequestParam("files[]") MultipartFile uploadFiles) {
		logger.info("App config file upload requested.");

		String dataPath = String.format("/opt/data/%s/%s/%s/conf", groupId, appId, name);
		boolean uploaded = false;
		String message = fileService.uploadAppConfigFile(dataPath, uploadFiles);
		
		if(message.equalsIgnoreCase("success"))
			uploaded = true;

		return new Response(uploaded, message).toString();
	}
}
