package ee.tuleva.epis.account

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

import java.time.Instant
import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AccountStatementControllerSpec extends BaseControllerSpec  {

    AccountStatementService accountStatementService = Mock(AccountStatementService)

    AccountStatementController controller = new AccountStatementController(accountStatementService)

    def "Getting an account statement works"() {
        given:
        List<FundBalance> sampleAccountStatement = []

        def mvc = mockMvc(controller)
        1 * accountStatementService.get(_) >> sampleAccountStatement

        expect:
        mvc.perform(get("/v1/account-statement"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json("[]"))
//                .andExpect(MockMvcResultMatchers.jsonPath('$.response.total.rate',
//                equals(sampleAccountStatement.response.total.rate)))
    }

    def "/v1/account-cash-flow-statement fetches transactions"() {
        given:
        CashFlowStatement sampleCashFlowStatement = new CashFlowStatement();

        def mvc = mockMvc(controller)
        1 * accountStatementService.getTransactions(_, _ as LocalDate, _ as LocalDate) >> sampleCashFlowStatement

        expect:
        mvc.perform(get("/v1/account-cash-flow-statement?from-date=2000-01-01&to-date=2010-01-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json("{}"))
    }

}
