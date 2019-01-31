package kr.co.inslab.codealley.filemanager.backup.config;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;
import kr.co.inslab.codealley.filemanager.backup.repository.BackupConfigRepository;
import kr.co.inslab.codealley.filemanager.backup.service.BackupService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Configuration
@EnableScheduling
public class BackupSchedulerConfig implements SchedulingConfigurer {
	private Log log = LogFactory.getLog(BackupSchedulerConfig.class);
	
    @Autowired
    BackupService backupService;
    
    @Autowired
    BackupConfigRepository backupConfigRepository;
    
    
	@Bean(destroyMethod="shutdown")
    public Executor taskScheduler() {
        //return Executors.newScheduledThreadPool(42);
		return Executors.newSingleThreadScheduledExecutor();
    }
    
    @Bean 
    ThreadPoolTaskScheduler threadPoolTaskScheduler() { 
    	return new ThreadPoolTaskScheduler(); 
	}
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                new Runnable() {
                    @Override public void run() {
                    	backupService.backup();
                    }
                },
                new Trigger() {
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
                }
        );
    }
}