package kr.co.inslab.codealley.filemanager.backup.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "conf_backup")
public class BackupConfigVO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String accessKeyId;
	private String secretAccessKey;
	private String bucketName;
	private String region;
	private String targetDir; 
	private String tmpDir;
	private String crontabExpression;
	private String description;
	
	@Id
	@Column(name = "id")
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAccessKeyId() {
		return accessKeyId;
	}
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}
	public String getSecretAccessKey() {
		return secretAccessKey;
	}
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}
	public String getBucketName() {
		return bucketName;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getTargetDir() {
		return targetDir;
	}
	public void setTargetDir(String targetDir) {
		this.targetDir = targetDir;
	}
	public String getTmpDir() {
		return tmpDir;
	}
	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}
	public String getCrontabExpression() {
		return crontabExpression;
	}
	public void setCrontabExpression(String crontabExpression) {
		this.crontabExpression = crontabExpression;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
