package ee.tuleva.epis.contact

import ee.tuleva.epis.BaseControllerSpec
import org.springframework.http.MediaType

import static ee.tuleva.epis.contact.ContactDetailsFixture.contactDetailsFixture
import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class ContactDetailsControllerSpec extends BaseControllerSpec {

    def contactDetailsService = Mock(ContactDetailsService)

    def controller = new ContactDetailsController(contactDetailsService)

    def mapper = objectMapper()

    def "Getting contact details works"() {
        given:
        ContactDetails contactDetails = contactDetailsFixture()
        1 * contactDetailsService.getContactDetails(_) >> contactDetails

        expect:
        mockMvc(controller)
            .perform(get("/v1/contact-details"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath('$.addressRow1', is(contactDetails.addressRow1)))
            .andExpect(jsonPath('$.addressRow2', is(contactDetails.addressRow2)))
            .andExpect(jsonPath('$.addressRow3', is(contactDetails.addressRow3)))
            .andExpect(jsonPath('$.country', is(contactDetails.country)))
            .andExpect(jsonPath('$.postalIndex', is(contactDetails.postalIndex)))
            .andExpect(jsonPath('$.districtCode', is(contactDetails.districtCode)))
            .andExpect(jsonPath('$.contactPreference', is(contactDetails.contactPreference.name())))
            .andExpect(jsonPath('$.languagePreference', is(contactDetails.languagePreference.name())))
            .andExpect(jsonPath('$.noticeNeeded', is(contactDetails.noticeNeeded)))
            .andExpect(jsonPath('$.email', is(contactDetails.email)))
            .andExpect(jsonPath('$.firstName', is(contactDetails.firstName)))
            .andExpect(jsonPath('$.lastName', is(contactDetails.lastName)))
            .andExpect(jsonPath('$.personalCode', is(contactDetails.personalCode)))
            .andExpect(jsonPath('$.phoneNumber', is(contactDetails.phoneNumber)))
            .andExpect(jsonPath('$.thirdPillarDistribution[0].activeThirdPillarFundIsin',
                is(contactDetails.thirdPillarDistribution[0].activeThirdPillarFundIsin)))
            .andExpect(jsonPath('$.thirdPillarDistribution[0].percentage',
                is(contactDetails.thirdPillarDistribution[0].percentage.toDouble())))
            .andExpect(jsonPath('$.pensionAccountNumber', is(contactDetails.pensionAccountNumber)))
    }

    def "Updating contact details works"() {
        given:
        ContactDetails contactDetails = contactDetailsFixture()
        1 * contactDetailsService.updateContactDetails(_, contactDetails)
        1 * contactDetailsService.getContactDetails(_) >> contactDetails

        expect:
        mockMvc(controller)
            .perform(post("/v1/contact-details")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(contactDetails)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))

            .andExpect(jsonPath('$.addressRow1', is(contactDetails.addressRow1)))
            .andExpect(jsonPath('$.addressRow2', is(contactDetails.addressRow2)))
            .andExpect(jsonPath('$.addressRow3', is(contactDetails.addressRow3)))
            .andExpect(jsonPath('$.country', is(contactDetails.country)))
            .andExpect(jsonPath('$.postalIndex', is(contactDetails.postalIndex)))
            .andExpect(jsonPath('$.districtCode', is(contactDetails.districtCode)))
            .andExpect(jsonPath('$.contactPreference', is(contactDetails.contactPreference.name())))
            .andExpect(jsonPath('$.languagePreference', is(contactDetails.languagePreference.name())))
            .andExpect(jsonPath('$.noticeNeeded', is(contactDetails.noticeNeeded)))
            .andExpect(jsonPath('$.email', is(contactDetails.email)))
            .andExpect(jsonPath('$.firstName', is(contactDetails.firstName)))
            .andExpect(jsonPath('$.lastName', is(contactDetails.lastName)))
            .andExpect(jsonPath('$.personalCode', is(contactDetails.personalCode)))
            .andExpect(jsonPath('$.phoneNumber', is(contactDetails.phoneNumber)))
            .andExpect(jsonPath('$.thirdPillarDistribution[0].activeThirdPillarFundIsin',
                is(contactDetails.thirdPillarDistribution[0].activeThirdPillarFundIsin)))
            .andExpect(jsonPath('$.thirdPillarDistribution[0].percentage',
                is(contactDetails.thirdPillarDistribution[0].percentage.toDouble())))
            .andExpect(jsonPath('$.pensionAccountNumber', is(contactDetails.pensionAccountNumber)))
    }

}
