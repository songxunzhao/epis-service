package ee.tuleva.epis.nav

import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.request.EpisMessage
import ee.tuleva.epis.epis.request.EpisMessageWrapper
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.EpisX17ResponseType
import ee.x_road.epis.producer.EpisX17Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement
import java.time.LocalDate

class NavServiceSpec extends Specification {

    EpisService episService
    EpisMessageWrapper episMessageWrapper
    EpisMessageResponseStore episMessageResponseStore
    EpisX17RequestFactory requestFactory
    EpisX17ResponseConverter responseConverter

    NavService navService

    def setup() {
        episService = Mock(EpisService)
        episMessageWrapper = Mock(EpisMessageWrapper)
        episMessageResponseStore = Mock(EpisMessageResponseStore)
        requestFactory = Mock(EpisX17RequestFactory)
        responseConverter = Mock(EpisX17ResponseConverter)
        navService = new NavService(episService, episMessageWrapper, episMessageResponseStore, requestFactory, responseConverter)
    }

    def "it fetches nav"() {
        given:
        def isin = "EE000"
        def date = LocalDate.parse("2019-01-01")

        JAXBElement request = Mock(JAXBElement)
        requestFactory.create(isin, date) >> request

        def episMessage = Mock(EpisMessage)
        episMessageWrapper.createWrappedMessage(request) >> episMessage
        episMessage.id >> '123'

        1 * episService.send(episMessage)

        def episData = Mock(EpisX17Type)
        episMessageResponseStore.pop('123', EpisX17Type.class) >> episData

        def episResponse = Mock(EpisX17ResponseType)
        episData.getResponse() >> episResponse

        def navData = Optional.of(new NavData(isin, date, 100.70))
        responseConverter.convert(episResponse) >> navData

        when:
        def response = navService.getNavData(isin, date)

        then:
        response == navData
    }
}
