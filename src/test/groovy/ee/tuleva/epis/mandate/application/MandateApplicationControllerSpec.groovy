package ee.tuleva.epis.mandate.application

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import java.time.Instant

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class MandateApplicationControllerSpec extends BaseControllerSpec {


    MandateApplicationListService mandateApplicationListService = Mock(MandateApplicationListService)

    MandateApplicationController controller = new MandateApplicationController(mandateApplicationListService)

    def "getting exchanges work"() {
        given:
        def mvc = mockMvc(controller)
        1 * mandateApplicationListService.get(_) >> sampleApplicationsResponse


        expect:
        mvc.perform(get("/v1/exchanges"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.[0].sourceFundIsin',
                is(sampleApplicationsResponse.first().sourceFundIsin)))
    }

    List<MandateExchangeApplicationResponse> sampleApplicationsResponse = Arrays.asList(
            MandateExchangeApplicationResponse.builder()
                    .sourceFundIsin("sampleSourceIsin")
                    .date(Instant.now())
                    .build()
    )
}