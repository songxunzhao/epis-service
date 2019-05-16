package ee.tuleva.epis.epis.converter

import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX5ResponseType
import ee.x_road.epis.producer.EpisX6ResponseType
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION
import static ee.tuleva.epis.mandate.application.MandateApplicationType.TRANSFER

class MandateResponseConverterSpec extends Specification {

    def converter = new MandateResponseConverter()
    def episMessageFactory = new EpisMessageFactory()

    def errorCode = 123
    def errorMessage = "Oops!"
    def processId = "asdfg"

    def "converts errored selection application responses"() {
        given:
        EpisX5ResponseType response = episMessageFactory.createEpisX5ResponseType()
        response.setResults(result(AnswerType.NOK, errorMessage, errorCode))

        when:
        def mandateResponse = converter.convert(response, processId)

        then:
        !mandateResponse.successful
        mandateResponse.applicationType == SELECTION
        mandateResponse.errorCode == errorCode
        mandateResponse.errorMessage == errorMessage
        mandateResponse.processId == processId
    }

    def "converts successful selection application responses"() {
        given:
        EpisX5ResponseType response = episMessageFactory.createEpisX5ResponseType()
        response.setResults(result(AnswerType.OK, null, null))

        when:
        def mandateResponse = converter.convert(response, processId)

        then:
        mandateResponse.successful
        mandateResponse.applicationType == SELECTION
        mandateResponse.errorCode == null
        mandateResponse.errorMessage == null
        mandateResponse.processId == processId
    }

    def "converts errored transfer application responses"() {
        given:
        EpisX6ResponseType response = episMessageFactory.createEpisX6ResponseType()
        response.setResults(result(AnswerType.NOK, errorMessage, errorCode))

        when:
        def mandateResponse = converter.convert(response, processId)

        then:
        !mandateResponse.successful
        mandateResponse.applicationType == TRANSFER
        mandateResponse.errorCode == errorCode
        mandateResponse.errorMessage == errorMessage
        mandateResponse.processId == processId
    }

    def "converts successful transfer application responses"() {
        given:
        EpisX6ResponseType response = episMessageFactory.createEpisX6ResponseType()
        response.setResults(result(AnswerType.OK, null, null))

        when:
        def mandateResponse = converter.convert(response, processId)

        then:
        mandateResponse.successful
        mandateResponse.applicationType == TRANSFER
        mandateResponse.errorCode == null
        mandateResponse.errorMessage == null
        mandateResponse.processId == processId
    }

    private static ResultType result(AnswerType answer, String errorMessage, Integer errorCode) {
        def result = new ResultType()
        result.setResult(answer)
        result.setErrorTextEng(errorMessage)
        result.setResultCode(errorCode)
        return result
    }

}
