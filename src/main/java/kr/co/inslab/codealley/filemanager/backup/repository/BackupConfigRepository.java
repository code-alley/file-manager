package kr.co.inslab.codealley.filemanager.backup.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BackupConfigRepository {
	private static Log log = LogFactory.getLog(BackupConfigRepository.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * 백업설정 테이블(conf_backup) 조회
	 * @return
	 */
	@Transactional(readOnly = true)
	public BackupConfigVO getBackupConfigVO() {
		BackupConfigVO backupConfigVO = null;
		
		try {
			TypedQuery<BackupConfigVO> query = entityManager.createQuery("SELECT A FROM BackupConfigVO A", BackupConfigVO.class);
			backupConfigVO = query.getSingleResult();
		} catch(Exception e) {
			log.error(e.getMessage());
		}
		
		return backupConfigVO;
	}
}
