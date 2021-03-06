package ee.tuleva.epis.account

import ee.tuleva.epis.config.UserPrincipal
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

import java.time.LocalDate

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev,cloudamqp")
class AccountStatementTest {

  @Autowired
  AccountStatementService service

  @Ignore
  @Test
  void testIt() {
      UserPrincipal principal = new UserPrincipal("36803028000", "Mati", "Maasikas")
      service.getAccountStatement(principal)
  }

  @Ignore
  @Test
  void testGetStatement() {
    String idCode = "45606246596"
    def startDate = LocalDate.of(2003,  1,  7)
    def endDate = LocalDate.of(2019, 5, 15)
    service.getCashFlowStatement(idCode, startDate, endDate)
  }
}
