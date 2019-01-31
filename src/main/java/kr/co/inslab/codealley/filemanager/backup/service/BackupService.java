package kr.co.inslab.codealley.filemanager.backup.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;
import kr.co.inslab.codealley.filemanager.backup.repository.BackupConfigRepository;
import kr.co.inslab.codealley.filemanager.backup.util.TarGzUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BackupService {
	private Log log = LogFactory.getLog(BackupService.class);
	
	@Autowired
	private AwsS3Service awsS3Service;
	
	@Autowired
	private BackupConfigRepository backupConfigRepository;
	
	private final String ext = ".tar.gz";
	/**
	 * 백업 스케쥴링
	 * @throws FileNotFoundException 
	 */
	public void backup() {
		this.backup(null);
	}
	
	public void backup(String groupName) {
		log.info("### backup start ##");
		
		BackupConfigVO backupConfigVO = backupConfigRepository.getBackupConfigVO();
		
		String targetDir = backupConfigVO.getTargetDir();
		String tmpDir = backupConfigVO.getTmpDir();
		
		//1. 백업 대상 디렉토리 조회
		List<File> groupDirList = new ArrayList<File>();
		
		if(groupName != null) {
			//그룹명 지정했을경우 해당 그룹폴더만
			File dataDir = new File(targetDir + groupName);
			groupDirList.add(dataDir);
			
		}else {
			//그룹명 지정 안했을경우 모든 그룹 폴더(/opt/data/..)
			File dataDir = new File(targetDir);
			groupDirList = this.getGroupDirs(dataDir);
		}
        
        log.info("groupDirList :: " + groupDirList);
        
        //2. 그룹별 디렉토리 압축 (/otp/data/testGroup -> /tmp/testGroup.zip)
        for(File groupDir : groupDirList){
        	try {
        		if(!groupDir.exists()){
        			throw new FileNotFoundException(groupDir.toString());
        		}
        		
        		String groupDirStr = groupDir.toString();
            	String groupNameStr = groupDirStr.substring(groupDirStr.lastIndexOf(File.separator) + 1);
        		
        		String tarGzName = tmpDir + groupNameStr + ext;
        		File tarGzFile = TarGzUtils.createTarGz(groupDir.toString(), tarGzName);
        		log.info("tarGzFile :: " + tarGzFile);
        		
        		//3. S3로 전송
            	awsS3Service.uploadObjectMultipart(tarGzFile);
            	
			} catch (IOException e) {
				log.error(e);
			}
        	
        }
        
        log.info("### backup completed ##\n");
	}
	
	/**
	 * 그룹 디렉토리 목록 조회
	 * @param dir 검색 대상 디렉토리
	 * @return
	 */
	public List<File> getGroupDirs(File dir) {
		List<File> groupDirList = new ArrayList<File>();
		File[] files = dir.listFiles();
		for(File file : files){
			if(file.isDirectory()) {
				groupDirList.add(file.getAbsoluteFile());
			}
        }
		return groupDirList;
	}
}
