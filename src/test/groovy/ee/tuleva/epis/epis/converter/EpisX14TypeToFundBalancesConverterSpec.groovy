package ee.tuleva.epis.epis.converter

import ee.tuleva.epis.account.FundBalance
import ee.tuleva.epis.epis.exception.EpisMessageException
import ee.tuleva.epis.epis.validator.EpisResultValidator
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import static ee.x_road.epis.producer.EpisX14ResponseType.Unit

class EpisX14TypeToFundBalancesConverterSpec extends Specification {

    def resultValidator = new EpisResultValidator()

    def converter = new EpisX14TypeToFundBalancesConverter(resultValidator)

    BigDecimal sampleAmount = new BigDecimal(2)
    BigDecimal sampleNav = new BigDecimal("0.64")
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"
    String sampleIsin3 = "EE3600109369"
    String sampleIsin4 = "EE3600007530"

    String sampleCurrency = "EUR"

    def "converts OK epis response"() {
        when:
        List<FundBalance> response = converter.convert(getSampleSource())

        then:
        response.size() == 3
        response[0].isin == sampleIsin1
        response[0].currency == sampleCurrency
        response[0].value == sampleAmount * sampleNav
        response[0].pillar == null
        response[0].units == sampleAmount
        response[0].nav == sampleNav

        response[1].isin == sampleIsin2
        response[1].currency == sampleCurrency
        response[1].value == sampleAmount * sampleNav * 2
        response[1].pillar == null
        response[1].units == sampleAmount * 2
        response[1].nav == sampleNav

        response[2].isin == sampleIsin3
        response[2].currency == sampleCurrency
        response[2].value == sampleAmount * sampleNav
        response[2].pillar == null
        response[2].units == sampleAmount
        response[2].nav == sampleNav
    }

    def "throws exception on NOK epis response"() {
        when:
        converter.convert(getSampleErrorResponse())

        then:
        thrown EpisMessageException
    }

    EpisX14Type getSampleSource() {
        def sampleUnitBegin = new Unit()
        sampleUnitBegin.setAmount(sampleAmount)
        sampleUnitBegin.setNAV(sampleNav)
        sampleUnitBegin.setISIN(sampleIsin1)
        sampleUnitBegin.setCurrency(sampleCurrency)
        sampleUnitBegin.setCode("BEGIN")

        def sampleUnitEnd = new Unit()
        sampleUnitEnd.setAmount(sampleAmount)
        sampleUnitEnd.setNAV(sampleNav)
        sampleUnitEnd.setISIN(sampleIsin1)
        sampleUnitEnd.setCurrency(sampleCurrency)
        sampleUnitEnd.setCode("END")

        def sampleUnitBron = new Unit()
        sampleUnitBron.setAmount(sampleAmount)
        sampleUnitBron.setNAV(sampleNav)
        sampleUnitBron.setISIN(sampleIsin2)
        sampleUnitBron.setCurrency(sampleCurrency)
        sampleUnitBron.setCode("END")
        sampleUnitBron.setAdditionalFeature("BRON")

        def sampleUnit = new Unit()
        sampleUnit.setAmount(sampleAmount)
        sampleUnit.setNAV(sampleNav)
        sampleUnit.setISIN(sampleIsin2)
        sampleUnit.setCurrency(sampleCurrency)
        sampleUnit.setCode("END")

        def sampleUnit2 = new Unit()
        sampleUnit2.setAmount(sampleAmount)
        sampleUnit2.setNAV(sampleNav)
        sampleUnit2.setISIN(sampleIsin3)
        sampleUnit2.setCurrency(sampleCurrency)
        sampleUnit2.setCode("END")

        def sampleUnit3 = new Unit()
        sampleUnit3.setAmount(sampleAmount)
        sampleUnit3.setISIN(sampleIsin4)
        sampleUnit3.setNAV(null)
        sampleUnit3.setCode("UFR")
        sampleUnit3.setComment("Osakute vahetus")
        sampleUnit3.setCurrency(sampleCurrency)

        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [sampleUnitBegin, sampleUnitEnd, sampleUnitBron, sampleUnit, sampleUnit2, sampleUnit3]
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
