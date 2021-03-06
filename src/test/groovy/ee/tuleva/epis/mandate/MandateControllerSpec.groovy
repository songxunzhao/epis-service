package ee.tuleva.epis.mandate

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import static ee.tuleva.epis.mandate.MandateCommandFixture.mandateCommandFixture
import static ee.tuleva.epis.mandate.MandateResponseFixture.mandateResponseFixture
import static ee.tuleva.epis.mandate.application.FundTransferExchangeFixture.fundTransferExchangeFixture
import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION
import static ee.tuleva.epis.mandate.application.MandateApplicationType.TRANSFER
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class MandateControllerSpec extends BaseControllerSpec {

    def mandateService = Mock(MandateService)

    def controller = new MandateController(mandateService)

    def mapper = objectMapper()

    def "Can POST mandates"() {
        given:
        def selectionProcessId = "selectionProcessId"
        def transferProcessId = "transferProcessId"

        def mandateCommand = mandateCommandFixture()
            .processId(selectionProcessId)
            .fundTransferExchanges([
                fundTransferExchangeFixture()
                    .processId(transferProcessId)
                    .build()
            ])
            .build()

        def sampleResponse = [
            mandateResponseFixture().applicationType(TRANSFER).processId(transferProcessId).build(),
            mandateResponseFixture().applicationType(SELECTION).processId(selectionProcessId).build()
        ]

        1 * mandateService.sendMandate(_, mandateCommand) >> sampleResponse

        expect:
        mockMvc(controller).perform(post("/v1/mandates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(mandateCommand)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))

            .andExpect(jsonPath('$.mandateResponses[0].successful', is(true)))
            .andExpect(jsonPath('$.mandateResponses[0].errorCode', is(null)))
            .andExpect(jsonPath('$.mandateResponses[0].errorMessage', is(null)))
            .andExpect(jsonPath('$.mandateResponses[0].applicationType', is(TRANSFER.name())))
            .andExpect(jsonPath('$.mandateResponses[0].processId', is(transferProcessId)))

            .andExpect(jsonPath('$.mandateResponses[1].successful', is(true)))
            .andExpect(jsonPath('$.mandateResponses[1].errorCode', is(null)))
            .andExpect(jsonPath('$.mandateResponses[1].errorMessage', is(null)))
            .andExpect(jsonPath('$.mandateResponses[1].applicationType', is(SELECTION.name())))
            .andExpect(jsonPath('$.mandateResponses[1].processId', is(selectionProcessId)))
    }
}
