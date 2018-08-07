package ee.tuleva.epis.fund

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import static ee.tuleva.epis.fund.Fund.FundStatus.ACTIVE
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class FundControllerSpec extends BaseControllerSpec {

  FundService fundService = Mock(FundService)

  FundController controller = new FundController(fundService)

  def mvc = mockMvc(controller)

  def "gets a list of pension funds"() {
    given:
    def fund = new Fund("EE3600109435", "Tuleva Maailma Aktsiate Pensionifond", "TUK75", 2, ACTIVE)

    1 * fundService.getPensionFunds() >> [fund]

    expect:
    mvc.perform(get("/v1/funds"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(jsonPath('$[0].isin', is(fund.isin)))
        .andExpect(jsonPath('$[0].name', is(fund.name)))
        .andExpect(jsonPath('$[0].shortName', is(fund.shortName)))
        .andExpect(jsonPath('$[0].pillar', is(fund.pillar)))
        .andExpect(jsonPath('$[0].status', is(fund.status.name())))
  }
}
