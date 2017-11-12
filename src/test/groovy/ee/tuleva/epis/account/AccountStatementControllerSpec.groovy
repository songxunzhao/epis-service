package ee.tuleva.epis.account

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

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
                .andExpect(MockMvcResultMatchers.content().json("[]"))
//                .andExpect(MockMvcResultMatchers.jsonPath('$.response.total.rate',
//                equals(sampleAccountStatement.response.total.rate)))
    }

}
