package ee.tuleva.epis.mandate.application

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

//    @Ignore
    @Test
    public void testIt(){
        String idCode = "38812022762"
        List<MandateExchangeApplicationResponse> response = service.get(idCode)
        response != null
    }
}
