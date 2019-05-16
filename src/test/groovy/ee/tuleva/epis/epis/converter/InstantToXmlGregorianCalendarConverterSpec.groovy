package ee.tuleva.epis.epis.converter

import spock.lang.Specification

import java.time.Instant

class InstantToXmlGregorianCalendarConverterSpec extends Specification {

    def converter = new InstantToXmlGregorianCalendarConverter()

    def "converts instant"() {
        given:
        def instant = Instant.parse("2019-05-16T15:13:27.141Z")

        when:
        def convertedInstant = converter.convert(instant)

        then:
        convertedInstant.normalize().toString() == "2019-05-16T15:13:27.141Z"
    }

}
