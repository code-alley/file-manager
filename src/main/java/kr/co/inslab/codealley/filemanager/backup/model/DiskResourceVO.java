package kr.co.inslab.codealley.filemanager.backup.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import kr.co.inslab.codealley.filemanager.app.utils.ModelUtils;

@Entity
@Table(name = "disk_resource")
@IdClass(DiskResourceVO.DiskResourceId.class)
public class DiskResourceVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id private String domain;
	@Id private String type;
	@Id private String instanceName;
	private long disk;
	
	@Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	
	public long getDisk() {
		return disk;
	}
	public void setDisk(long disk) {
		this.disk = disk;
	}
	
	@PrePersist
	@PreUpdate
	protected void onUpdate() {
		updateDate = new Date();
	}
	
	public String toString() {
		return ModelUtils.toString(this);
	}

	static class DiskResourceId implements Serializable{
		private static final long serialVersionUID = 1L;
		
		private String domain;
		private String type;
		private String instanceName;
		
		public String getDomain() {
			return domain;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getInstanceName() {
			return instanceName;
		}
		public void setInstanceName(String instanceName) {
			this.instanceName = instanceName;
		}
	}
	
}
