package ee.tuleva.epis.fund

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.fund.converter.EpisX18TypeToFundListConverter
import ee.x_road.epis.producer.EpisX18Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.fund.Fund.FundStatus.ACTIVE

class FundServiceSpec extends Specification {

  EpisService episService
  EpisMessageResponseStore episMessageResponseStore
  EpisMessageWrapper episMessageWrapper
  EpisMessageFactory episMessageFactory
  EpisX18TypeToFundListConverter converter

  FundService fundService

  def setup() {
    episService = Mock(EpisService)
    episMessageResponseStore = Mock(EpisMessageResponseStore)
    episMessageWrapper = Mock(EpisMessageWrapper)
    episMessageFactory = new EpisMessageFactory()
    converter = Mock(EpisX18TypeToFundListConverter)

    fundService = new FundService(episService, episMessageResponseStore, episMessageWrapper, episMessageFactory, converter)
  }

  def "get a list of pension funds"() {
    given:
    EpisX18Type sampleResponse = new EpisX18Type()

    List<Fund> sampleFunds = [
        new Fund("EE3600109435", "Tuleva Maailma Aktsiate Pensionifond", "TUK75", 2, ACTIVE),
    ]

    1 * episMessageWrapper.wrap(_ as String, _ as JAXBElement)
    1 * episMessageResponseStore.pop(_, EpisX18Type.class) >> sampleResponse
    1 * converter.convert(sampleResponse) >> sampleFunds

    when:
    List<Fund> funds = fundService.getPensionFunds()

    then:
    funds == sampleFunds
  }
}
