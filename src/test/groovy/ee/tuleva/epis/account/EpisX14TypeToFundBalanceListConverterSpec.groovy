package ee.tuleva.epis.account

import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import spock.lang.Specification

class EpisX14TypeToFundBalanceListConverterSpec extends Specification {

    EpisX14TypeToFundBalanceListConverter converter = new EpisX14TypeToFundBalanceListConverter();

    BigDecimal sampleAmount = new BigDecimal(100)
    BigDecimal sampleNav = new BigDecimal("0.64")
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"
    String sampleCurrency = "EUR"

    def "Converts EpisX14Type to List of Fund"() {

        when:
        List<FundBalance> response = converter.convert(getSampleSource())

        then:
        response.size() == 2
        response.first().isin == sampleIsin1
        response.first().currency == sampleCurrency
        response.first().value == sampleAmount.multiply(sampleNav)
        response.first().pillar == 2
        response.last().isin == sampleIsin2
        response.last().currency == sampleCurrency
        response.last().value == sampleAmount.multiply(sampleNav)
        response.last().pillar == 2
    }

    EpisX14Type getSampleSource() {
        EpisX14ResponseType.Unit sampleUnitThatWillRepeat = new EpisX14ResponseType.Unit()
        sampleUnitThatWillRepeat.setAmount(sampleAmount)
        sampleUnitThatWillRepeat.setNAV(sampleNav)
        sampleUnitThatWillRepeat.setISIN(sampleIsin1)
        sampleUnitThatWillRepeat.setCurrency(sampleCurrency)

        EpisX14ResponseType.Unit sampleUnit = new EpisX14ResponseType.Unit()
        sampleUnit.setAmount(sampleAmount)
        sampleUnit.setNAV(sampleNav)
        sampleUnit.setISIN(sampleIsin2)
        sampleUnit.setCurrency(sampleCurrency)

        EpisX14ResponseType episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [sampleUnitThatWillRepeat, sampleUnitThatWillRepeat, sampleUnit]
        })

        EpisX14Type sampleSource = new EpisX14Type()
        sampleSource.setResponse(episX14ResponseType)

        return sampleSource
    }

}
