package ee.tuleva.epis.nav

import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter
import ee.x_road.epis.producer.EpisX17ResponseType
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory
import java.time.LocalDate

import static ee.x_road.epis.producer.AnswerType.NOK
import static ee.x_road.epis.producer.AnswerType.OK

class EpisX17ResponseConverterSpec extends Specification {

    EpisX17ResponseConverter converter

    EpisX17ResponseType response = Mock(EpisX17ResponseType)
    ResultType responseResult = Mock(ResultType)

    def setup() {
        converter = new EpisX17ResponseConverter()
        response.results >> responseResult
    }

    def "converts EPIS response"() {
        given:
        responseResult.result >> OK
        response.ISIN >> "EE111"
        response.validityDate >> DatatypeFactory.newInstance().newXMLGregorianCalendar("2018-12-30T22:00:00Z")
        response.NAV >> 100.69
        when:
        def result = converter.convert(response)
        then:
        result.isPresent()
        result.get().isin == "EE111"
        result.get().date == LocalDate.parse("2018-12-31")
        result.get().value == 100.69
    }

    def "converts NOK EPIS response to empty result"() {
        given:
        responseResult.result >> NOK
        when:
        def result = converter.convert(response)
        then:
        !result.isPresent()
    }
}
