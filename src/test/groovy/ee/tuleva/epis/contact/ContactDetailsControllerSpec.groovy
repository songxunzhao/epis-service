package ee.tuleva.epis.contact

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import static ee.tuleva.epis.contact.ContactDetailsFixture.contactDetailsFixture
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class ContactDetailsControllerSpec extends BaseControllerSpec {

    ContactDetailsService contactDetailsService = Mock(ContactDetailsService)

    ContactDetailsController controller = new ContactDetailsController(contactDetailsService)

    def "Getting contact details works"() {
        given:
        ContactDetails sampleContactDetails = contactDetailsFixture()

        def mvc = mockMvc(controller)
        1 * contactDetailsService.get(_) >> sampleContactDetails

        expect:
        mvc.perform(get("/v1/contact-details"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath('$.addressRow1', is(sampleContactDetails.addressRow1)))
            .andExpect(jsonPath('$.addressRow2', is(sampleContactDetails.addressRow2)))
            .andExpect(jsonPath('$.addressRow3', is(sampleContactDetails.addressRow3)))
            .andExpect(jsonPath('$.country', is(sampleContactDetails.country)))
            .andExpect(jsonPath('$.postalIndex', is(sampleContactDetails.postalIndex)))
            .andExpect(jsonPath('$.districtCode', is(sampleContactDetails.districtCode)))
            .andExpect(jsonPath('$.contactPreference', is(sampleContactDetails.contactPreference.name())))
            .andExpect(jsonPath('$.languagePreference', is(sampleContactDetails.languagePreference.name())))
            .andExpect(jsonPath('$.noticeNeeded', is(sampleContactDetails.noticeNeeded)))
            .andExpect(jsonPath('$.email', is(sampleContactDetails.email)))
            .andExpect(jsonPath('$.firstName', is(sampleContactDetails.firstName)))
            .andExpect(jsonPath('$.lastName', is(sampleContactDetails.lastName)))
            .andExpect(jsonPath('$.personalCode', is(sampleContactDetails.personalCode)))
            .andExpect(jsonPath('$.phoneNumber', is(sampleContactDetails.phoneNumber)))
            .andExpect(jsonPath('$.thirdPillarDistribution[0].activeThirdPillarFundIsin',
                is(sampleContactDetails.thirdPillarDistribution[0].activeThirdPillarFundIsin)))
            .andExpect(jsonPath('$.thirdPillarDistribution[0].percentage',
                is(sampleContactDetails.thirdPillarDistribution[0].percentage.toDouble())))
    }

}
