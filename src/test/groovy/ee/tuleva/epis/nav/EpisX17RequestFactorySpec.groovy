package ee.tuleva.epis.nav


import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter
import spock.lang.Specification

import javax.xml.datatype.XMLGregorianCalendar
import java.time.LocalDate

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory

class EpisX17RequestFactorySpec extends Specification {

    EpisMessageFactory episMessageFactory
    LocalDateToXmlGregorianCalendarConverter dateConverter

    EpisX17RequestFactory factory

    def setup() {
        episMessageFactory = new EpisMessageFactory()
        dateConverter = Mock(LocalDateToXmlGregorianCalendarConverter)
        factory = new EpisX17RequestFactory(episMessageFactory, dateConverter)
    }

    def "create request"() {
        given:
        def isin = "EE000"
        def date = LocalDate.parse("2019-01-01")

        def xmlDate = Mock(XMLGregorianCalendar)
        dateConverter.convert(date) >> xmlDate

        when:
        def request = factory.create(isin, date)

        then:
        request.value.request.ISIN == isin
        request.value.request.validityDate == xmlDate
    }
}
