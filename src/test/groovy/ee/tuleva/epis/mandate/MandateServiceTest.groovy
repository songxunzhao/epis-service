package ee.tuleva.epis.mandate

import ee.tuleva.epis.account.AccountStatementService
import ee.tuleva.epis.account.FundBalance
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

import java.time.Instant

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev,cloudamqp")
class MandateServiceTest {

    @Autowired
    MandateService mandateService

    @Autowired
    ContactDetailsService contactDetailsService

    @Autowired
    AccountStatementService accountStatementService

    @Test
    @Ignore
    void testFutureContributionsApplication() {
        String personalCode = "45606246596"
        ContactDetails contactDetails = contactDetailsService.get(personalCode)

        def mandateCommand = MandateCommand.builder()
            .id(123L)
            .pillar(2)
            .createdDate(Instant.now())
            .futureContributionFundIsin("EE3600109435")
            .processId("dsafdas")
            .build()

        mandateService.sendSelectionApplication(contactDetails, mandateCommand)
    }

    @Test
    @Ignore
    void testFundTransferApplication() {
        String personalCode = "45606246596"
        ContactDetails contactDetails = contactDetailsService.get(personalCode)

        def mandateCommand = MandateCommand.builder()
            .id(123L)
            .pillar(2)
            .createdDate(Instant.now())
            .fundTransferExchanges([
                new FundTransferExchange(1.0, "EE3600019774", "EE3600109435", "asdadsfds")
            ])
            .build()

        mandateService.sendFundTransferApplications(contactDetails, mandateCommand)
    }

    @Test
    @Ignore
    void testSendFullMandateFor2ndPillar() {
        String personalCode = "45606246596"
        def mandateCommand = MandateCommand.builder()
            .id(123L)
            .pillar(2)
            .createdDate(Instant.now())
            .fundTransferExchanges([
                new FundTransferExchange(1.0, "EE3600019774", "EE3600109435", "asdadsfds")
            ])
            .futureContributionFundIsin("EE3600109435")
            .processId("dsafdas")
            .build()

        mandateService.sendMandate(personalCode, mandateCommand)
    }

    @Test
    @Ignore
    void testSendFundTransferApplicationFor3rdPillar() {
        String personalCode = "39107050268"
        String processId = UUID.randomUUID().toString().replace("-", "");
        def sourceIsin = "EE3600071049"

        def accountStatement = accountStatementService.getAccountStatement(personalCode)
        FundBalance balance = getFundBalance(accountStatement, sourceIsin)

        def mandateCommand = MandateCommand.builder()
            .id(123L)
            .pillar(3)
            .createdDate(Instant.now())
            .fundTransferExchanges([
                new FundTransferExchange(balance.units, sourceIsin, "EE3600109419", processId)
            ])
            .build()

        mandateService.sendMandate(personalCode, mandateCommand)
    }

    private static FundBalance getFundBalance(List<FundBalance> accountStatement, String isin) {
        return accountStatement.stream()
            .filter({ fundBalance -> (fundBalance.isin == isin) })
            .findFirst()
            .orElseThrow({ -> new IllegalStateException("Fund not found: " + isin) })
    }

}
