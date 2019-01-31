package kr.co.inslab.codealley.filemanager.backup.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import kr.co.inslab.codealley.filemanager.backup.config.BackupSchedulerConfig;
import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;
import kr.co.inslab.codealley.filemanager.backup.model.DiskResourceVO;
import kr.co.inslab.codealley.filemanager.backup.repository.BackupConfigRepository;
import kr.co.inslab.codealley.filemanager.backup.repository.ResourceRepository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
@Service
public class ResourceService {
	private Log log = LogFactory.getLog(BackupSchedulerConfig.class);
	
	@Autowired
	private BackupConfigRepository backupConfigRepository;
	
	@Autowired
	private ResourceRepository resourceRepository;
	
	@RequestMapping(value="/resource/diskscheduler")
	@Scheduled(cron = "0 0 0/6 * * ?") 
	public void diskScheduler() {
		log.info("## diskScheduler start ##");
		
		//사용량 조회
		List<DiskResourceVO> diskResourceList = this.getDiskResource();
		
		//DB 입력
		resourceRepository.updateDiskResource(diskResourceList);
		
		log.info("## diskScheduler completed ##\n");
	}

	/**
	 * 그룹>툴>인스턴스명 별로 디스크 사용량 조회
	 * @return
	 */
	public List<DiskResourceVO> getDiskResource() {
		
		BackupConfigVO backupConfigVO = backupConfigRepository.getBackupConfigVO();
		
		File targetDir = new File(backupConfigVO.getTargetDir());
		
		List<DiskResourceVO> diskResourceList = new ArrayList<DiskResourceVO>();
		
		for(File groups : targetDir.listFiles()){ //그룹 목록
			if(groups.isDirectory()) {
				for(File tools : groups.listFiles()) { //툴목록
					if(tools.isDirectory()) {
						for(File instance : tools.listFiles()) { //툴하위 인스턴스명 목록
							if(instance.isDirectory()) {
								DiskResourceVO resource = new DiskResourceVO();
								
								resource.setDomain(groups.getName());
								resource.setType(tools.getName());
								resource.setInstanceName(instance.getName());
								
								long disk = 0l;
								
								//디렉토리 사이즈 조회
								if(SystemUtils.IS_OS_LINUX) {
									//리눅스일경우 du 명렁어를 실행해 '디스크 할당 크기' 를 가져온다.
									Process p = null;
									try {
										p = Runtime.getRuntime().exec("du -sb " + instance.getAbsolutePath());
										String line;
										BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()) );
										p.waitFor();
										if ((line = reader.readLine()) != null) {
											//log.info("line 1 :: " + line);
											//log.info("line 3 :: " + line.split("\\s+")[0]);
											disk = Long.parseLong(line.split("\\s+")[0]);
										}
										
										reader.close();
										p.destroy();
										
									} catch (IOException | InterruptedException e) {
										log.error(e);
										disk = FileUtils.sizeOfDirectory(new File(instance.getAbsolutePath()));
									} 
									
								} else {
									//Windows 일경우 일단 단순 디렉토리 사이즈를 사용한다.(실서버는 리눅스일테니..)
									//디스크 할당크기 가져오려면 추가 배치파일 작업이 필요할듯..
									disk = FileUtils.sizeOfDirectory(new File(instance.getAbsolutePath()));
								}
								
								resource.setDisk(disk);
								
								diskResourceList.add(resource);
							}
						}
					}
				}
			}
			
        }
		log.info("diskResourceList.size()" +  diskResourceList.size());
		return diskResourceList;
	}
}
