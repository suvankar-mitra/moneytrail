package cc.suvankar.moneytrail;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MoneytrailApplicationTests {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

  @Test
  void contextLoads() {}
}
