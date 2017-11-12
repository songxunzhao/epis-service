package ee.tuleva.epis.contact

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.EpisX12Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory

class ContactDetailsServiceSpec extends Specification {

    EpisService episService = Mock(EpisService);
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore);
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    ContactDetailsConverter contactDetailsConverter = Mock(ContactDetailsConverter)
    EpisMessageFactory episMessageFactory = new EpisMessageFactory()
    ContactDetailsService service = new ContactDetailsService(episService,
            episMessageResponseStore, episMessageWrapper, contactDetailsConverter, episMessageFactory)

    def "Get contact details"() {
        given:
        String personalCode = "38080808080"
        EpisX12Type sampleEpisResponse = new EpisX12Type()
        ContactDetails sampleContactDetails = ContactDetails.builder().addressRow1("someAddress").build()

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        })

        episMessageResponseStore.pop(_, EpisX12Type.class) >> sampleEpisResponse
        contactDetailsConverter.toContactDetails(sampleEpisResponse) >> sampleContactDetails

        when:
        ContactDetails contactDetails = service.get(personalCode)

        then:
        contactDetails == sampleContactDetails
    }

}
