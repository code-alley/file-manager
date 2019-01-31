package kr.co.inslab.codealley.filemanager.app.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import kr.co.inslab.codealley.filemanager.app.model.AppConfigFile;

@Repository("FileService")
public class FileService {

	/**
	 * App config file 가져오기
	 * 
	 * @param groupId
	 * @param name
	 * @param filePath
	 * @return
	 */
	public List<AppConfigFile> getAppConfigFile(String filePath) {
		List<AppConfigFile> files = new ArrayList<AppConfigFile>();
		File dir = new File(filePath);
		if (dir.isDirectory()) {
			File[] fileList = dir.listFiles();

			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				if (file.isFile()) {
					System.out.println("file name : " + file.getName());
					AppConfigFile appConfigFile = new AppConfigFile();
					appConfigFile.setName(file.getName());

					files.add(appConfigFile);
				}
			}
		}

		return files;
	}

	/**
	 * App config file 다운로드
	 * 
	 * @param response
	 * @param filePath
	 * @param fileName
	 * @return
	 */
	public boolean downloadAppConfigFile(HttpServletResponse response, String filePath, String fileName) {
		File file = new File(filePath + "/" + fileName);
		boolean downloaded = false;

		response.setContentType("application/download;");
		response.setHeader("Content-Disposition", "attachment;" + " filename=\"" + fileName + "\";");
		int length = (int) file.length();
		response.setContentLength(length);

		FileInputStream fis = null;

		try {
			FileCopyUtils.copy(new FileInputStream(file), response.getOutputStream());
			downloaded = true;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return downloaded;
	}

	/**
	 * App config file 업로드
	 * 
	 * @param groupId
	 * @param name
	 * @param filePath
	 * @param files
	 * @return
	 */
	public String uploadAppConfigFile(String filePath, MultipartFile files) {
		String message = null;
		String fileName = files.getOriginalFilename();

		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		File file = new File(filePath + "/" + fileName);

		if (file.isFile()) {
			try {
				fileOutputStream = new FileOutputStream(file);
				bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
				bufferedOutputStream.write(files.getBytes());
				message = "Success";

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (bufferedOutputStream != null)
						bufferedOutputStream.close();
					if (fileOutputStream != null)
						fileOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			message = "This file cannot be uploaded properly!";
		}

		return message;
	}
}
