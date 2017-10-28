package ee.tuleva.epis.contact

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
class ContactDetailsServiceTest {

    @Autowired
    ContactDetailsService service;

    @Ignore
    @Test
    public void testIt(){
        String idCode = "38812022762"
        service.get(idCode)
    }
}
