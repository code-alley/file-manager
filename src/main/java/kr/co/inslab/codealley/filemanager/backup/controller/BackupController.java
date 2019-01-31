package kr.co.inslab.codealley.filemanager.backup.controller;

import java.util.Date;

import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;
import kr.co.inslab.codealley.filemanager.backup.repository.BackupConfigRepository;
import kr.co.inslab.codealley.filemanager.backup.service.BackupService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backup")
public class BackupController{
	private Log log = LogFactory.getLog(BackupController.class);
	
	@Autowired 
	private ThreadPoolTaskScheduler threadPoolTaskScheduler; 
	
    @Autowired
    BackupService backupService;
    
    @Autowired
    BackupConfigRepository backupConfigRepository;
    
    /**
     * 백업 스케줄 중지
     * @return
     */
	@RequestMapping("/stop") 
	@ResponseBody
	public String stop() { 
		log.info("BackupController stop");
		this.threadPoolTaskScheduler.getScheduledThreadPoolExecutor().shutdownNow();
		return "Backup scheduling has been stopped.";
	} 
	
	/**
     * 백업 스케줄 시작
     * @return
     */
	@RequestMapping("/start")
	@ResponseBody
	public String start() { 
		log.info("BackupController start");
		
		if(this.threadPoolTaskScheduler.getScheduledThreadPoolExecutor().isShutdown()) {
			this.threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
			this.threadPoolTaskScheduler.initialize();
			this.threadPoolTaskScheduler.schedule(new Runnable() {
				@Override public void run() {
					backupService.backup();
	            }
			}
	        , new Trigger() {
            	//백업 주기 적용을 위한 트리거, DB에서 cron 표현식 조회
                @Override
                public Date nextExecutionTime(TriggerContext triggerContext) {
                	BackupConfigVO backupConfigVO = backupConfigRepository.getBackupConfigVO();
                	String cronTabExpression = backupConfigVO.getCrontabExpression();
                	log.info("cronTabExpression :: " + cronTabExpression);
                	
                	CronTrigger trigger = new CronTrigger(cronTabExpression);
                    Date nextExec = trigger.nextExecutionTime(triggerContext);
                    return nextExec;
                }
            });
		}
		return "Backup scheduling has been started.";
	} 
	
	/**
	 * 백업 스케줄 중지후 재시작
	 */
	@RequestMapping("/restart")
	public String restart() {
		this.stop();
		return this.start();
	}
	
	/**
	 * 백업 바로 실행
	 */
	@RequestMapping("/run")
	public void run() {
		backupService.backup();
	}
	
	/**
	 * 백업 바로 실행(특정 그룹 지정)
	 */
	@RequestMapping("/run/{groupName}")
	public void runGroupBackup(@PathVariable("groupName") String groupName) {
		backupService.backup(groupName);
	}
	
}
