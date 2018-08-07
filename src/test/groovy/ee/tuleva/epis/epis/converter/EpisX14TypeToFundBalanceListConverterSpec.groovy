package ee.tuleva.epis.epis.converter

import ee.tuleva.epis.account.FundBalance
import ee.tuleva.epis.epis.exception.EpisMessageException
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

class EpisX14TypeToFundBalanceListConverterSpec extends Specification {

    def converter = new EpisX14TypeToFundBalanceListConverter()

    BigDecimal sampleAmount = new BigDecimal(2)
    BigDecimal sampleNav = new BigDecimal("0.64")
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"
    String sampleIsin3 = "EE3600109369"
    String sampleCurrency = "EUR"

    def "converts OK epis response"() {
        when:
        List<FundBalance> response = converter.convert(getSampleSource())

        then:
        response.size() == 2
        response.first().isin == sampleIsin1
        response.first().currency == sampleCurrency
        response.first().value == sampleAmount * sampleNav
        response.first().pillar == 2
        response.last().isin == sampleIsin2
        response.last().currency == sampleCurrency
        response.last().value == sampleAmount * sampleNav
        response.last().pillar == 2
    }

    def "throws exception on NOK epis response"() {
        when:
        converter.convert(getSampleErrorResponse())

        then:
        thrown EpisMessageException
    }

    EpisX14Type getSampleSource() {
        def sampleUnitThatWillRepeat = new EpisX14ResponseType.Unit()
        sampleUnitThatWillRepeat.setAmount(sampleAmount)
        sampleUnitThatWillRepeat.setNAV(sampleNav)
        sampleUnitThatWillRepeat.setISIN(sampleIsin1)
        sampleUnitThatWillRepeat.setCurrency(sampleCurrency)

        def sampleUnit = new EpisX14ResponseType.Unit()
        sampleUnit.setAmount(sampleAmount)
        sampleUnit.setNAV(sampleNav)
        sampleUnit.setISIN(sampleIsin2)
        sampleUnit.setCurrency(sampleCurrency)

        def sampleUnitThatWillBeIgnored = new EpisX14ResponseType.Unit()
        sampleUnitThatWillBeIgnored.setAmount(sampleAmount)
        sampleUnitThatWillBeIgnored.setNAV(sampleNav)
        sampleUnitThatWillBeIgnored.setISIN(sampleIsin3)
        sampleUnitThatWillBeIgnored.setCurrency(sampleCurrency)

        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [sampleUnitThatWillRepeat, sampleUnitThatWillRepeat, sampleUnit, sampleUnitThatWillBeIgnored]
            getResults() >> result
        })

        def source = new EpisX14Type()
        source.setResponse(episX14ResponseType)

        return source
    }

    EpisX14Type getSampleErrorResponse() {
        def result = new ResultType()
        result.result = AnswerType.NOK
        result.errorTextEng = "Error!!!1"
        result.resultCode = 1234

        def response = Mock(EpisX14ResponseType, {
            getResults() >> result
        })

        def source = new EpisX14Type()
        source.setResponse(response)

        return source
    }

}
