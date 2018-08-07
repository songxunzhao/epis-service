package ee.tuleva.epis.fund.converter

import ee.tuleva.epis.fund.Fund
import ee.x_road.epis.producer.EpisX18ResponseType
import ee.x_road.epis.producer.EpisX18Type
import ee.x_road.epis.producer.SecurityStatusType
import spock.lang.Specification

import static ee.tuleva.epis.fund.Fund.FundStatus.ACTIVE
import static ee.x_road.epis.producer.EpisX18ResponseType.*


class EpisX18TypeToFundListConverterSpec extends Specification {

  def converter = new EpisX18TypeToFundListConverter()

  def "converts a singleton fund list"() {
    given:
    def security = new Security()
    security.ISIN = "EE3600109435"
    security.name = "Tuleva Maailma Aktsiate Pensionifond"
    security.shortName = "TUK75"
    security.pillar = "2"
    security.status = SecurityStatusType.A

    def response = new EpisX18ResponseType()
    response.getSecurity().add(security)

    def episX18Type = new EpisX18Type()
    episX18Type.setResponse(response)

    when:
    def funds = converter.convert(episX18Type)

    then:
    funds == [
        Fund.builder()
            .isin(security.ISIN)
            .name(security.name)
            .shortName(security.shortName)
            .pillar(2)
            .status(ACTIVE)
            .build()
    ]
  }

}
