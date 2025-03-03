package eckofox.EFbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EFboxApplication {

	public static void main(String[] args) {
		System.out.println("THIS APPLICATION IS NOT CACHEABLE.");
		try {
			Thread.sleep(3000);
		} catch (Exception e) {
			e.getMessage();
		}
		SpringApplication.run(EFboxApplication.class, args);
	}

}
