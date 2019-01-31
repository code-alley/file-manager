package kr.co.inslab.codealley.filemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodealleyFilemanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodealleyFilemanagerApplication.class, args);
	}
}
