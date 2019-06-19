package ee.tuleva.epis.mandate

import ee.tuleva.epis.account.AccountStatementService
import ee.tuleva.epis.account.FundBalance
import ee.tuleva.epis.config.UserPrincipal
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
        UserPrincipal principal = new UserPrincipal("45606246596", "Mari", "Maasikas")
        ContactDetails contactDetails = contactDetailsService.getContactDetails(principal)

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
        UserPrincipal principal = new UserPrincipal("45606246596", "Mari", "Maasikas")
        ContactDetails contactDetails = contactDetailsService.getContactDetails(principal)

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
        UserPrincipal principal = new UserPrincipal("45606246596", "Mari", "Maasikas")
        String transferProcessId = UUID.randomUUID().toString().replace("-", "")
        String selectionProcessId = UUID.randomUUID().toString().replace("-", "");

        def mandateCommand = MandateCommand.builder()
            .id(123L)
            .pillar(2)
            .createdDate(Instant.now())
            .fundTransferExchanges([
                new FundTransferExchange(1.0, "EE3600019774", "EE3600109435", transferProcessId)
            ])
            .futureContributionFundIsin("EE3600109435")
            .processId(selectionProcessId)
            .build()

        mandateService.sendMandate(principal, mandateCommand)
    }

    @Test
    @Ignore
    void testSendFullMandateApplicationFor3rdPillar() {
        UserPrincipal principal = new UserPrincipal("45606246596", "Mari", "Maasikas")
        String transferProcessId = UUID.randomUUID().toString().replace("-", "")
        String selectionProcessId = UUID.randomUUID().toString().replace("-", "");
        def sourceIsin = "EE3600071049"

        FundBalance balance = getFundBalance(sourceIsin, principal)

        def mandateCommand = MandateCommand.builder()
            .id(123L)
            .pillar(3)
            .createdDate(Instant.now())
            .fundTransferExchanges([
                new FundTransferExchange(balance.units, sourceIsin, "EE3600109419", transferProcessId)
            ])
            .futureContributionFundIsin("EE3600109419")
            .processId(selectionProcessId)
            .build()

        mandateService.sendMandate(principal, mandateCommand)
    }

    private FundBalance getFundBalance(String isin, UserPrincipal principal) {
        def accountStatement = accountStatementService.getAccountStatement(principal)
        return accountStatement.stream()
            .filter({ fundBalance -> (fundBalance.isin == isin) })
            .findFirst()
            .orElseThrow({ -> new IllegalStateException("Fund not found: " + isin) })
    }

}
