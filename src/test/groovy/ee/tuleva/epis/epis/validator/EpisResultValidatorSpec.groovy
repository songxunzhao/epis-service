package ee.tuleva.epis.epis.validator

import ee.tuleva.epis.epis.exception.EpisMessageException
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

class EpisResultValidatorSpec extends Specification {

    def validator = new EpisResultValidator()

    def "Throws exception on NOT-OK (NOK) response"() {
        given:
        def result = new ResultType()
        result.result = AnswerType.NOK

        when:
        validator.validate(result)

        then:
        thrown EpisMessageException
    }

    def "OK response passes validation"() {
        given:
        def result = new ResultType()
        result.result = AnswerType.OK

        when:
        validator.validate(result)

        then:
        true
    }
}
