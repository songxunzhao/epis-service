package ee.tuleva.epis.account

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import java.time.Instant
import java.time.LocalDate

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class AccountStatementControllerSpec extends BaseControllerSpec {

    AccountStatementService accountStatementService = Mock(AccountStatementService)

    AccountStatementController controller = new AccountStatementController(accountStatementService)

    def "Getting an account statement works"() {
        given:
        def sampleFundBalance = FundBalance.builder()
            .isin("EE3600109435")
            .value(new BigDecimal("1.12"))
            .currency('EUR')
            .pillar(2)
            .units(BigDecimal.ONE)
            .nav(new BigDecimal("1.12"))
            .activeContributions(true)

            .build()
        List<FundBalance> sampleAccountStatement = [sampleFundBalance]

        def mvc = mockMvc(controller)
        1 * accountStatementService.getAccountStatement(_) >> sampleAccountStatement

        expect:
        mvc.perform(get("/v1/account-statement"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath('$[0].isin', is(sampleFundBalance.isin)))
            .andExpect(jsonPath('$[0].value', is(sampleFundBalance.value.doubleValue())))
            .andExpect(jsonPath('$[0].currency', is(sampleFundBalance.currency)))
            .andExpect(jsonPath('$[0].pillar', is(sampleFundBalance.pillar)))
            .andExpect(jsonPath('$[0].activeContributions', is(sampleFundBalance.activeContributions)))
            .andExpect(jsonPath('$[0].units', is(sampleFundBalance.units)))
            .andExpect(jsonPath('$[0].nav', is(sampleFundBalance.nav)))

    }

    def "/v1/account-cash-flow-statement fetches transactions"() {
        given:
        def isin = "EE3600109435"
        def transaction = Transaction.builder()
            .time(Instant.now())
            .pillar(2)
            .currency("EUR")
            .amount(new BigDecimal("1.23"))
            .build()

        def sampleCashFlowStatement = CashFlowStatement.builder()
            .startBalance([(isin): transaction])
            .endBalance([(isin): transaction])
            .transactions([transaction])
            .build()

        def mvc = mockMvc(controller)
        1 * accountStatementService.getCashFlowStatement(_, _ as LocalDate, _ as LocalDate) >> sampleCashFlowStatement

        expect:
        mvc.perform(get("/v1/account-cash-flow-statement?from-date=2000-01-01&to-date=2010-01-01"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath('$.startBalance.EE3600109435.amount', is(transaction.amount.doubleValue())))
            .andExpect(jsonPath('$.endBalance.EE3600109435.amount', is(transaction.amount.doubleValue())))
            .andExpect(jsonPath('$.transactions[0].amount', is(transaction.amount.doubleValue())))
    }

}
