package ee.tuleva.epis.mandate

import ee.tuleva.epis.contact.ContactDetails
import ee.tuleva.epis.contact.ContactDetailsService
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
class MandateServiceTest {

    @Autowired
    MandateService mandateService

    @Autowired
    ContactDetailsService contactDetailsService

    @Test
    @Ignore
    void testIt() {
        String personalCode = "45606246596"
        String futureContributionFundIsin = "EE3600109443"
        Integer pillar = 2
        LocalDate documentDate = LocalDate.now()
        String documentNumber = "123"
        ContactDetails contactDetails = contactDetailsService.get(personalCode)

        mandateService.sendFutureContributionsApplication(
            contactDetails, futureContributionFundIsin, pillar, documentDate, documentNumber
        )
    }
}
