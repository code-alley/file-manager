package kr.co.inslab.codealley.filemanager.backup.service;

import java.io.File;

import kr.co.inslab.codealley.filemanager.backup.model.BackupConfigVO;
import kr.co.inslab.codealley.filemanager.backup.repository.BackupConfigRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

@Service
public class AwsS3Service {
	
	private static Log log = LogFactory.getLog(AwsS3Service.class);
	
	@Autowired
	private BackupConfigRepository backupConfigRepository;
	
	private BasicAWSCredentials creds;
	private AmazonS3 s3Client;
	private String bucketName;
	
	private long loggingTime;
	
	/**
	 * S3인증정보 셋팅	
	 */
	public void setCredentials() {
		BackupConfigVO backupConfigVO = backupConfigRepository.getBackupConfigVO();
		String accessKeyId = backupConfigVO.getAccessKeyId();
		String secretAccessKey = backupConfigVO.getSecretAccessKey();
		String region = backupConfigVO.getRegion();
		
		this.creds = new BasicAWSCredentials(accessKeyId, secretAccessKey); 
		this.s3Client = AmazonS3ClientBuilder.standard()
        		.withCredentials(new AWSStaticCredentialsProvider(creds))
        		//.withRegion(Regions.US_WEST_2)
        		.withRegion(region)
        		.build();
		
		this.bucketName = backupConfigVO.getBucketName();
	}
	
	/**
	 * S3에 파일 업로드(멀티파트)
	 * @param file
	 */
	public void uploadObjectMultipart(File file) {
		log.info("## uploadObjectMultipart start ##");
		this.setCredentials();
		
		try {
			TransferManager tm =  TransferManagerBuilder.standard().withS3Client(s3Client).build();
			
	        log.info("Uploading a new object to S3["+bucketName+"] from a file[" + file + "]");
	        
			final Upload upload = tm.upload(bucketName, file.getName(), file);
			
			//진행상황을 보기위한 리스너 등록
	        upload.addProgressListener(new ProgressListener() {
	            public void progressChanged(ProgressEvent progressEvent) {
	            	long now = System.currentTimeMillis();
	            	
	            	//완료 여부
	            	boolean completed = progressEvent.getEventType().equals(ProgressEventType.TRANSFER_COMPLETED_EVENT);
	            	
	            	//5초에 한번씩 혹은 전송 완료되었을시 로깅(바이트 수와 퍼센트)
	            	if((loggingTime <= (now - 5000)) || completed){
	            		log.info("progressEvent :: " +
		            			"Bytes[" + upload.getProgress().getBytesTransferred() + "] " +
		            			"Percent[" + String.format("%.2f", upload.getProgress().getPercentTransferred()) + "]"
		            			);
	            		loggingTime = now;
	            	}
	            	
	            	if (completed) {
	            		log.info("progressEvent :: upload complete !");
	                }
	            }
	        });
			
	        upload.waitForCompletion();
	        tm.shutdownNow(false);
	        
		}catch (AmazonClientException|InterruptedException e) {
        	log.error(e);
        }

		//로깅시간이 밀리초까지 똑같을경우 로깅순서가 뒤바뀔수가 있네
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		log.info("## uploadObjectMultipart completed ##\n");
	}
	
	/**
	 * S3에 파일 업로드(단일 작업)
	 * @param file
	 */
	public void uploadObject(File file) {
		log.info("## uploadObject start ##");
		this.setCredentials();
		
		try {
			
	        log.info("Uploading a new object to S3["+bucketName+"] from a file[" + file + "]");
	        s3Client.putObject(new PutObjectRequest(bucketName, file.getName(), file));
	        
		} catch (AmazonClientException e) {
        	log.error(e);
        }
		
		log.info("## uploadObject completed ##\n");
	}
	
	/**
	 * S3파일을 가져와 임시 폴더에 다운로드
	 * @param fileName
	 * @param tmpDir
	 * @return
	 */
	public File getObject(String fileName, String tmpDir) throws Exception {
		log.info("## getObject start ##");
		this.setCredentials();
		
		File localFile = new File(tmpDir + "S3."+ fileName);
		ObjectMetadata object = s3Client.getObject(new GetObjectRequest(bucketName, fileName), localFile);
		
		log.info("bucketName: " + bucketName);
		log.info("fileName: " + fileName);
		log.info("localFile: " + localFile);
		log.info("ContentType :: " + object.getContentType());
		log.info("ContentLength :: " + object.getContentLength());
		log.info("LastModified :: " + object.getLastModified());
		
		log.info("## getObject completed ##\n");
		
		return localFile;
	}
}
