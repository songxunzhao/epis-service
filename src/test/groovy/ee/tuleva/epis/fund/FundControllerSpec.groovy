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
    def stockFund = new Fund("EE3600109435", "Tuleva Maailma Aktsiate Pensionifond", "TUK75", 2, ACTIVE)
    def bondFund = new Fund("EE3600109443", "Tuleva Maailma Võlakirjade Pensionifond", "TUK00", 2, ACTIVE)

    1 * fundService.getPensionFunds() >> [stockFund, bondFund]

    expect:
    mvc.perform(get("/v1/funds"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(jsonPath('$[0].isin', is(stockFund.isin)))
        .andExpect(jsonPath('$[0].name', is(stockFund.name)))
        .andExpect(jsonPath('$[0].shortName', is(stockFund.shortName)))
        .andExpect(jsonPath('$[0].pillar', is(stockFund.pillar)))
        .andExpect(jsonPath('$[0].status', is(stockFund.status.name())))

        .andExpect(jsonPath('$[1].isin', is(bondFund.isin)))
        .andExpect(jsonPath('$[1].name', is(bondFund.name)))
        .andExpect(jsonPath('$[1].shortName', is(bondFund.shortName)))
        .andExpect(jsonPath('$[1].pillar', is(bondFund.pillar)))
        .andExpect(jsonPath('$[1].status', is(bondFund.status.name())))
  }
}
