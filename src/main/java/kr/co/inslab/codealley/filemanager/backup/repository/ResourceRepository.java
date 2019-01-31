package kr.co.inslab.codealley.filemanager.backup.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import kr.co.inslab.codealley.filemanager.backup.model.DiskResourceVO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ResourceRepository {
	private static Log log = LogFactory.getLog(ResourceRepository.class);
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * Disk 사용량 입력 
	 * @param diskResourceList
	 */
	public void updateDiskResource(List<DiskResourceVO> diskResourceList) {
		for(DiskResourceVO vo : diskResourceList) {
			log.info("Update DiskResource : " + vo.toString());
			entityManager.merge(vo);
		}
	}
}
