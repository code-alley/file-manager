package kr.co.inslab.codealley.filemanager.backup.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;
import kr.co.inslab.codealley.filemanager.backup.repository.BackupConfigRepository;
import kr.co.inslab.codealley.filemanager.backup.util.TarGzUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RestoreService {
	private Log log = LogFactory.getLog(RestoreService.class);

	@Autowired
	private AwsS3Service awsS3Service;
	
	@Autowired
	BackupConfigRepository backupConfigRepository;
	
	private String targetDir;
	private String tmpDir;
	
	private final String ext = ".tar.gz";
	
//	@Autowired
//	public RestoreService(){
//		BackupConfigVO backupConfigVO = backupConfigRepository.getBackupConfigVO();
//		this.targetDir = backupConfigVO.getTargetDir();
//		this.tmpDir= backupConfigVO.getTmpDir();
//	}
	
	/**
	 * 백업 대상 및 임시 디렉토리 지정
	 */
	public void setDir() {
		BackupConfigVO backupConfigVO = backupConfigRepository.getBackupConfigVO();
		this.targetDir = backupConfigVO.getTargetDir();
		this.tmpDir= backupConfigVO.getTmpDir();
	}
	
	/**
	 * S3에서 그룹별 백업 파일 가져와 대상 폴더에 압축 해제
	 * @param groupName
	 * @param toolName
	 * @throws Exception
	 */
	public void restore(String groupName, String toolName) throws Exception {
		log.info(String.format("## restore start groupName[%s] toolName[%s] ##", groupName, toolName));
		
		this.setDir();
		
		//1.현재 사용중인 폴더 압축후 임시 저장
		boolean compressResult = this.compressGroupDir(groupName, toolName);
		
		if(compressResult) {
			
			//2. S3파일 임시 폴더로 다운로드
	    	String fileName = groupName + ext;
			File localFile = awsS3Service.getObject(fileName, tmpDir);
			
			//3. 현재 사용중인 폴더 삭제
			this.removeGroupDir(groupName, toolName);
			
			//4. 다운받은 S3파일 압축 해제
			this.decompressGroupFile(localFile, groupName, toolName);
		}

		log.info("## restore completed ##\n");
	}
	
	/**
	 * 현재 사용중인 그룹 폴더 압축후 temp 폴더에 저장
	 * @param groupName
	 */
	public boolean compressGroupDir(String groupName, String toolName) {
		log.info("## compressGroupDir start ##");
		
		boolean compressResult = false;
		try {
			String groupDir = targetDir + groupName;
			
			//폴더 존재여부 확인
			String tdir = toolName != null ? groupDir + "/" + toolName : groupDir;
    		if(!new File(tdir).exists()){
    			throw new FileNotFoundException(tdir);
    		}
    		
    		String tarGzName = tmpDir + groupName + ext;
    		File tarGzFile = TarGzUtils.createTarGz(groupDir, tarGzName);
    		log.info("tarGzFile :: " + tarGzFile);
    		
    		compressResult = true;
    		
		} catch (IOException e) {
			log.error(e);
		}
    	
    	log.info("## compressGroupDir completed ##");
    	return compressResult;
	}
	
	/**
	 * 그룹 폴더 압축 해제
	 * @param filename
	 * @param groupName
	 * @param toolName
	 */
	public void decompressGroupFile(File filename, String groupName, String toolName) {
		log.info("## decompressGroupFile start ##");
		
		String groupDir = targetDir + groupName;
		
		try {
			if(toolName == null) {
				//그룹 폴더 전체 압축 해제
				TarGzUtils.extract(filename, groupDir);
			} else {
				//특정 툴만 압축 해제
				String toolDir = toolName + "/";
				TarGzUtils.extract(filename, groupDir, toolDir);
			}
			
		} catch (IOException e) {
			log.error(e);
		}
			
		log.info("## decompressGroupFile completed ##");
	}
	
	/**
	 * 디렉토리 삭제
	 * @param groupName
	 * @param toolName
	 */
	public void removeGroupDir(String groupName, String toolName) {
		log.info(String.format("## removeGroupDir start groupName[%s] toolName[%s] ##", groupName, toolName));
		try {
			String rmTargetDir = targetDir + groupName;
			
			//툴 네임이 있으면 해당 툴 폴더만 삭제
			rmTargetDir = toolName != null ? rmTargetDir + "/" + toolName : rmTargetDir;
			log.info("rmTargetDir : "+ rmTargetDir);
			
			FileUtils.deleteDirectory(new File(rmTargetDir));
			
		} catch (IOException e) {
			log.error(e);
		}
		log.info("## removeGroupDir completed ##");
	}
}
