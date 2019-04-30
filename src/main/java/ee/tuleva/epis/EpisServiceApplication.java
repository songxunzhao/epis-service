package ee.tuleva.epis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EpisServiceApplication {

    public static void main(String[] args) {
        // CloudFlare is not a fan of Java user agents
        System.setProperty("http.agent", "HTTPie/1.0.2");
        SpringApplication.run(EpisServiceApplication.class, args);
    }

}
