package ee.tuleva.epis.mandate

import ee.tuleva.epis.contact.ContactDetailsService
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.converter.ContactDetailsToAddressTypeConverter
import ee.tuleva.epis.epis.converter.ContactDetailsToPersonalDataConverter
import ee.tuleva.epis.epis.converter.InstantToXmlGregorianCalendarConverter
import ee.tuleva.epis.epis.converter.MandateResponseConverter
import ee.tuleva.epis.epis.request.EpisMessageWrapper
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.*
import spock.lang.Specification

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.contact.ContactDetailsFixture.contactDetailsFixture
import static ee.tuleva.epis.mandate.MandateCommandFixture.mandateCommandFixture
import static ee.tuleva.epis.mandate.application.FundTransferExchangeFixture.fundTransferExchangeFixture
import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION
import static ee.tuleva.epis.mandate.application.MandateApplicationType.TRANSFER

class ThirdPillarMandateServiceSpec extends Specification {

    def episService = Mock(EpisService)
    def responseStore = Mock(EpisMessageResponseStore)
    def messageWrapper = Mock(EpisMessageWrapper)
    def messageFactory = new EpisMessageFactory()
    def timeConverter = new InstantToXmlGregorianCalendarConverter()
    def contactDetailsService = Mock(ContactDetailsService)
    def responseConverter = new MandateResponseConverter()
    def personaDataConverter = new ContactDetailsToPersonalDataConverter(messageFactory)
    def addressConverter = new ContactDetailsToAddressTypeConverter(messageFactory)

    def service = new ThirdPillarMandateService(episService, responseStore, messageWrapper, messageFactory,
        timeConverter, contactDetailsService, responseConverter, personaDataConverter, addressConverter)

    def "can successfully send a 3rd pillar mandate"() {
        given:
        def personalCode = "38080808080"
        def mandateCommand = mandateCommandFixture()
            .pillar(3)
            .fundTransferExchanges([
                fundTransferExchangeFixture()
                    .amount(123.456)
                    .build()
            ])
            .build()

        1 * contactDetailsService.getContactDetails(personalCode) >> contactDetailsFixture()
        1 * responseStore.pop(_, EpisX31Type.class) >> episX31Type(episX31Response(result(AnswerType.OK)))
        1 * responseStore.pop(_, EpisX37Type.class) >> episX37Type(episX37Response(result(AnswerType.OK)))

        when:
        def mandateResponses = service.sendMandate(personalCode, mandateCommand)

        then:
        mandateResponses[0].successful
        mandateResponses[0].applicationType == TRANSFER
        mandateResponses[0].processId == mandateCommand.fundTransferExchanges[0].processId
        mandateResponses[0].errorCode == null
        mandateResponses[0].errorMessage == null

        mandateResponses[1].successful
        mandateResponses[1].applicationType == SELECTION
        mandateResponses[1].processId == mandateCommand.processId
        mandateResponses[1].errorCode == null
        mandateResponses[1].errorMessage == null
    }

    private EpisX37Type episX37Type(EpisX37ResponseType response) {
        EpisX37Type episX37Type = messageFactory.createEpisX37Type()
        episX37Type.setResponse(response)
        return episX37Type
    }

    private static EpisX37ResponseType episX37Response(ResultType result) {
        EpisX37ResponseType response = new EpisX37ResponseType()
        response.setResults(result)
        return response
    }

    private EpisX31Type episX31Type(EpisX31ResponseType response) {
        EpisX31Type episX31Type = messageFactory.createEpisX31Type()
        episX31Type.setResponse(response)
        return episX31Type
    }

    private static EpisX31ResponseType episX31Response(ResultType result) {
        EpisX31ResponseType response = new EpisX31ResponseType()
        response.setResults(result)
        return response
    }

    private static ResultType result(AnswerType answer) {
        ResultType result = new ResultType()
        result.setResult(answer)
        return result
    }

}
