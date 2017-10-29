package ee.tuleva.epis.mandate.application

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
class MandateApplicationListServiceTest {

    @Autowired
    MandateApplicationListService service;

    @Ignore
    @Test
    public void testIt(){
        String idCode = "38812022762" // has second pillar funds
//        String idCode = "49909121121" // no second pillar
//        String idCode = "49909121688" // empty second pillar
        List<MandateExchangeApplicationResponse> response = service.get(idCode)
        response != null
    }
}
