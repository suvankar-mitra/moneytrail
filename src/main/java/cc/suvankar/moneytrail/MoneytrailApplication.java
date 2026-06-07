package cc.suvankar.moneytrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.*;

@SpringBootApplication
@EnableSpringDataWebSupport(
    pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MoneytrailApplication {

  public static void main(String[] args) {
    SpringApplication.run(MoneytrailApplication.class, args);
  }
}
