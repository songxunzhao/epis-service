package ee.tuleva.epis.account

import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter
import ee.tuleva.epis.epis.request.EpisMessageWrapper
import ee.x_road.epis.producer.EpisX14RequestType
import ee.x_road.epis.producer.EpisX14Type
import ee.x_road.epis.producer.PersonDataRequestType
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory

class AccountStatementRequestFactorySpec extends Specification {

    def episMessageWrapper = Mock(EpisMessageWrapper)
    def episMessageFactory = Mock(EpisMessageFactory)
    def dateConverter = new LocalDateToXmlGregorianCalendarConverter()
    def clock = Clock.fixed(Instant.parse("2020-08-23T10:00:00Z"), ZoneOffset.UTC)
    AccountStatementRequestFactory requestFactory

    def setup() {
        requestFactory = new AccountStatementRequestFactory(episMessageWrapper, episMessageFactory, dateConverter,
            clock)
    }

    def "sets correct start and end date to the account statement request"() {
        given:
        def personalCode = "385321443243"
        def startDate = LocalDate.of(2020, 8, 1)
        def endDate = LocalDate.of(2020, 8, 24)
        def request = new EpisX14RequestType()
        episMessageFactory.createPersonDataRequestType() >> new PersonDataRequestType()
        episMessageFactory.createEpisX14RequestType() >> request
        episMessageFactory.createEpisX14Type() >> new EpisX14Type()

        when:
        requestFactory.buildQuery(personalCode, startDate, endDate)

        then:
        request.startDate == dateConverter.convert(startDate)
        request.endDate == dateConverter.convert(endDate)
    }

    def "sets default start and end date when they are missing or null"() {
        given:
        def personalCode = "385321443243"
        def startDate = null
        def endDate = null
        def request = new EpisX14RequestType()
        episMessageFactory.createPersonDataRequestType() >> new PersonDataRequestType()
        episMessageFactory.createEpisX14RequestType() >> request
        episMessageFactory.createEpisX14Type() >> new EpisX14Type()

        when:
        requestFactory.buildQuery(personalCode, startDate, endDate)

        then:
        request.startDate == dateConverter.convert(LocalDate.of(2000, 1, 1))
        request.endDate == dateConverter.convert(LocalDate.now(clock))
    }
}
