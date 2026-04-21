package tn.esprit.jungleinenglishbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class JungleInEnglishBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JungleInEnglishBackendApplication.class, args);
	}
}
