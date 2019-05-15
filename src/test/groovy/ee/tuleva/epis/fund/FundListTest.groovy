package ee.tuleva.epis.fund

import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev,cloudamqp")
class FundListTest {

  @Autowired
  FundService service

  @Test
  @Ignore
  public void testIt() {
    def funds = service.getPensionFunds()
  }

}
