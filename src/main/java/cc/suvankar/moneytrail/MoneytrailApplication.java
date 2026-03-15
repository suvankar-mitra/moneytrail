package cc.suvankar.moneytrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoneytrailApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneytrailApplication.class, args);
	}

}
