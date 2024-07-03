package ru.sfedu.mmcs_nexus;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.sfedu.mmcs_nexus.config.DotenvConfig;

@SpringBootApplication
public class MMCSNexusApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory("./")
				.load();

		System.out.println(dotenv.get("GITHUB_CLIENT_ID"));
		SpringApplication.run(MMCSNexusApplication.class, args);
	}

}
