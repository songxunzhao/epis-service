package ee.tuleva.epis.mandate

import ee.tuleva.epis.contact.ContactDetailsService
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.converter.InstantToXmlGregorianCalendarConverter
import ee.tuleva.epis.epis.converter.MandateResponseConverter
import ee.tuleva.epis.epis.request.EpisMessageWrapper
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.*
import spock.lang.Specification

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.contact.ContactDetailsFixture.contactDetailsFixture
import static ee.tuleva.epis.mandate.MandateCommandFixture.mandateCommandFixture
import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION
import static ee.tuleva.epis.mandate.application.MandateApplicationType.TRANSFER

class MandateServiceSpec extends Specification {

    def episService = Mock(EpisService)
    def responseStore = Mock(EpisMessageResponseStore)
    def messageWrapper = Mock(EpisMessageWrapper)
    def messageFactory = new EpisMessageFactory()
    def timeConverter = new InstantToXmlGregorianCalendarConverter()
    def contactDetailsService = Mock(ContactDetailsService)
    def responseConverter = new MandateResponseConverter()

    def service = new MandateService(episService, responseStore, messageWrapper, messageFactory, timeConverter,
        contactDetailsService, responseConverter)

    def "can successfully send mandate"() {
        given:
        def personalCode = "38080808080"
        def mandateCommand = mandateCommandFixture()
        1 * contactDetailsService.get(personalCode) >> contactDetailsFixture()
        1 * responseStore.pop(_, EpisX6Type.class) >> episX6Type(episX6Response(result(AnswerType.OK)))
        1 * responseStore.pop(_, EpisX5Type.class) >> episX5Type(episX5Response(result(AnswerType.OK)))

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

    private EpisX5Type episX5Type(EpisX5ResponseType response) {
        EpisX5Type episX5Type = messageFactory.createEpisX5Type()
        episX5Type.setResponse(response)
        return episX5Type
    }

    private static EpisX5ResponseType episX5Response(ResultType result) {
        EpisX5ResponseType response = new EpisX5ResponseType()
        response.setResults(result)
        return response
    }

    private EpisX6Type episX6Type(EpisX6ResponseType response) {
        EpisX6Type episX6Type = messageFactory.createEpisX6Type()
        episX6Type.setResponse(response)
        return episX6Type
    }

    private static EpisX6ResponseType episX6Response(ResultType result) {
        EpisX6ResponseType response = new EpisX6ResponseType()
        response.setResults(result)
        return response
    }

    private static ResultType result(AnswerType answer) {
        ResultType result = new ResultType()
        result.setResult(answer)
        return result
    }

}
