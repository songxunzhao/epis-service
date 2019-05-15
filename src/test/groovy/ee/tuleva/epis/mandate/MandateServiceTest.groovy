package ee.tuleva.epis.mandate

import ee.tuleva.epis.contact.ContactDetails
import ee.tuleva.epis.contact.ContactDetailsService
import ee.tuleva.epis.mandate.application.FundTransferExchange
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
    void testFutureContributionsApplication() {
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

    @Test
    @Ignore
    void testFundTransferApplication() {
        String personalCode = "45606246596"
        List<FundTransferExchange> fundTransferExchanges = [
            new FundTransferExchange(1.0, "EE3600019774", "EE3600109435")
        ]
        Integer pillar = 2
        LocalDate documentDate = LocalDate.now()
        String documentNumber = "123"
        ContactDetails contactDetails = contactDetailsService.get(personalCode)

        mandateService.sendFundTransferApplication(
            contactDetails, fundTransferExchanges, pillar, documentDate, documentNumber
        )
    }
}
