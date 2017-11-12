package ee.tuleva.epis.account

import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import spock.lang.Specification

class EpisX14TypeToFundBalanceListConverterSpec extends Specification {

    EpisX14TypeToFundBalanceListConverter converter = new EpisX14TypeToFundBalanceListConverter();

    BigDecimal sampleAmount = new BigDecimal(2)
    BigDecimal samplePrice = new BigDecimal(0.63)
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"
    String sampleCurrency = "EUR"

    def "Convert"() {

        when:
        List<FundBalance> response = converter.convert(getSampleSource())

        then:
        response.size() == 2
        response.first().isin == sampleIsin1
        response.first().currency == sampleCurrency
        response.first().value == sampleAmount * samplePrice
        response.first().pillar == 2
        response.last().isin == sampleIsin2
        response.last().currency == sampleCurrency
        response.last().value == sampleAmount * samplePrice
        response.last().pillar == 2
    }

    EpisX14Type getSampleSource() {
        EpisX14ResponseType.Unit sampleUnitThatWillRepeat = new EpisX14ResponseType.Unit()
        sampleUnitThatWillRepeat.setAmount(sampleAmount)
        sampleUnitThatWillRepeat.setPrice(samplePrice)
        sampleUnitThatWillRepeat.setISIN(sampleIsin1)
        sampleUnitThatWillRepeat.setCurrency(sampleCurrency)

        EpisX14ResponseType.Unit sampleUnit = new EpisX14ResponseType.Unit()
        sampleUnit.setAmount(sampleAmount)
        sampleUnit.setPrice(samplePrice)
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
