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
@ActiveProfiles("dev,cloudamqp")
class ContactDetailsServiceTest {

    @Autowired
    ContactDetailsService service

    @Test
    @Ignore
    void testGetContactDetails() {
        String idCode = "45606246596"
        service.getContactDetails(idCode)
    }

    @Test
    @Ignore
    void testUpdateContactDetails() {
        String idCode = "45606246596"
        ContactDetails contactDetails = service.getContactDetails(idCode)
        contactDetails.addressRow1 = "asdfg"
        contactDetails.email = "test@gmail.com"
        service.updateContactDetails(idCode, contactDetails)
    }

}
