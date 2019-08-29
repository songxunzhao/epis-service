package ee.tuleva.epis.nav

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import java.time.LocalDate

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class NavControllerSpec extends BaseControllerSpec {

    def navService = Mock(NavService)
    def controller = new NavController(navService)
    def mvc = mockMvc(controller)

    def "gets nav"() {
        given:
        def isin = "EE999"
        def date = LocalDate.parse("2019-06-06")
        def navData = new NavData(isin, date, 44.55)
        navService.getNavData(isin, date) >> Optional.of(navData)
        expect:
        mvc.perform(get("/v1/navs/${isin}?date=${date}"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath('isin', is(isin)))
            .andExpect(jsonPath('date', is(date.toString())))
            .andExpect(jsonPath('value', is(44.55.doubleValue())))
    }
}
