package ee.tuleva.epis.contact

import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.converter.ContactDetailsToAddressTypeConverter
import ee.tuleva.epis.epis.converter.ContactDetailsToPersonalDataConverter
import ee.tuleva.epis.epis.converter.InstantToXmlGregorianCalendarConverter
import ee.tuleva.epis.epis.request.EpisMessageWrapper
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.epis.validator.EpisResultValidator
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.EpisX4ResponseType
import ee.x_road.epis.producer.EpisX4Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import javax.xml.bind.JAXBElement

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.contact.ContactDetailsFixture.contactDetailsFixture

class ContactDetailsServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    ContactDetailsConverter contactDetailsConverter = Mock(ContactDetailsConverter)
    EpisMessageFactory episMessageFactory = new EpisMessageFactory()
    def personalDataConverter = new ContactDetailsToPersonalDataConverter(episMessageFactory)
    def addressConverter = new ContactDetailsToAddressTypeConverter(episMessageFactory)
    def timeConverter = new InstantToXmlGregorianCalendarConverter()
    def resultValidator = new EpisResultValidator()
    ContactDetailsService service = new ContactDetailsService(episService,
            episMessageResponseStore, episMessageWrapper, contactDetailsConverter, episMessageFactory,
        personalDataConverter, addressConverter, timeConverter, resultValidator)

    def "Can get contact details"() {
        given:
        String personalCode = "38080808080"
        EpisX12Type sampleEpisResponse = new EpisX12Type()
        ContactDetails sampleContactDetails = contactDetailsFixture()

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        })

        episMessageResponseStore.pop(_, EpisX12Type.class) >> sampleEpisResponse
        contactDetailsConverter.convert(sampleEpisResponse) >> sampleContactDetails

        when:
        ContactDetails contactDetails = service.getContactDetails(personalCode)

        then:
        contactDetails == sampleContactDetails
    }

    def "Can update contact details"() {
        given:
        def sampleEpisResponse = okEpisResponse()
        ContactDetails sampleContactDetails = contactDetailsFixture()
        String personalCode = sampleContactDetails.personalCode

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX4Type> updatePersonalDataRequest ->

            def requestPersonalCode = updatePersonalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        })

        episMessageResponseStore.pop(_, EpisX4Type.class) >> sampleEpisResponse

        when:
        service.updateContactDetails(personalCode, sampleContactDetails)

        then:
        true
    }

    private EpisX4Type okEpisResponse() {
        def result = new ResultType()
        result.setResult(AnswerType.OK)

        def response = new EpisX4ResponseType()
        response.setResults(result)

        def episResponse = new EpisX4Type()
        episResponse.setResponse(response)

        return episResponse
    }

}
