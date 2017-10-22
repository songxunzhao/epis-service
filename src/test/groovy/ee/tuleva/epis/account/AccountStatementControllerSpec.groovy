package ee.tuleva.epis.account

import ee.tuleva.epis.BaseControllerSpec
import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import org.springframework.http.MediaType

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AccountStatementControllerSpec extends BaseControllerSpec  {

    AccountStatementService accountStatementService = Mock(AccountStatementService)

    AccountStatementController controller = new AccountStatementController(accountStatementService)

    def "Getting an account statement works"() {
        given:
        EpisX14Type sampleAccountStatement = sampleAccountStatement()

        def mvc = mockMvc(controller)
        1 * accountStatementService.get(_) >> sampleAccountStatement

        expect:
        mvc.perform(get("/v1/account-statement"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//                .andExpect(MockMvcResultMatchers.jsonPath('$.response.total.rate',
//                equals(sampleAccountStatement.response.total.rate)))
    }

    EpisX14Type sampleAccountStatement() {
        EpisX14ResponseType response = new EpisX14ResponseType();

        EpisX14ResponseType.Total total = new EpisX14ResponseType.Total()
        total.setRate(1)
        response.setTotal(total)

        EpisX14Type samplePerson = new EpisX14Type();
        samplePerson.setResponse(response)

        return samplePerson;
    }

}
