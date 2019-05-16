package ee.tuleva.epis.epis.converter

import spock.lang.Specification

import java.time.LocalDate

class LocalDateToXmlGregorianCalendarConverterSpec extends Specification {

    def converter = new LocalDateToXmlGregorianCalendarConverter()

    def "converts local date properly"() {
        given:
        def localDate = LocalDate.of(2019, 5, 16)

        when:
        def convertedDate = converter.convert(localDate)

        then:
        convertedDate.toString() == "2019-05-16"
    }
}
