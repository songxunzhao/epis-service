package ee.tuleva.epis.account

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
class AccountStatementTest {

  @Autowired
  AccountStatementService service;

  @Ignore
  @Test
  public void testIt() {
    String idCode = "38812022762"
    service.get(idCode)
  }

  @Ignore
  @Test
  public void testGetStatement() {
    String idCode = "38812022762"
    GregorianCalendar startDate = new GregorianCalendar();
    startDate.set(2003,  0,  7);
    GregorianCalendar endDate = new GregorianCalendar();
    endDate.set(2018, 06, 15);
    service.getTransactions(idCode, startDate, endDate)
  }
}
