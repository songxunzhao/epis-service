package ee.tuleva.epis.epis.converter

import ee.tuleva.epis.account.FundBalance
import ee.tuleva.epis.epis.exception.EpisMessageException
import ee.tuleva.epis.epis.validator.EpisResultValidator
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import java.math.RoundingMode

import static ee.x_road.epis.producer.EpisX14ResponseType.Unit

class EpisX14TypeToFundBalancesConverterSpec extends Specification {

    def resultValidator = new EpisResultValidator()

    def converter = new EpisX14TypeToFundBalancesConverter(resultValidator)

    BigDecimal sampleAmount = 2.0
    BigDecimal sampleNav = 0.6456
    BigDecimal sampleValue = (sampleAmount * sampleNav).setScale(2, RoundingMode.HALF_UP)
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

        with(response[0]) {
            isin == sampleIsin1
            currency == sampleCurrency
            value == sampleValue
            unavailableValue == 0.0
            pillar == null
            units == sampleAmount
            unavailableUnits == 0.0
            nav == sampleNav
        }

        with(response[1]) {
            isin == sampleIsin2
            currency == sampleCurrency
            value == sampleValue
            unavailableValue == sampleValue
            pillar == null
            units == sampleAmount
            unavailableUnits == sampleAmount
            nav == sampleNav
        }

        with(response[2]) {
            isin == sampleIsin3
            currency == sampleCurrency
            value == sampleValue
            unavailableValue == 0.0
            pillar == null
            units == sampleAmount
            unavailableUnits == 0.0
            nav == sampleNav
        }
    }

    def "throws exception on NOK epis response"() {
        when:
        converter.convert(getSampleErrorResponse())

        then:
        thrown EpisMessageException
    }

    EpisX14Type getSampleSource() {
        def sampleUnit1Begin = new Unit()
        sampleUnit1Begin.setAmount(sampleAmount)
        sampleUnit1Begin.setNAV(sampleNav)
        sampleUnit1Begin.setISIN(sampleIsin1)
        sampleUnit1Begin.setCurrency(sampleCurrency)
        sampleUnit1Begin.setCode("BEGIN")

        def sampleUnit1End = new Unit()
        sampleUnit1End.setAmount(sampleAmount)
        sampleUnit1End.setNAV(sampleNav)
        sampleUnit1End.setISIN(sampleIsin1)
        sampleUnit1End.setCurrency(sampleCurrency)
        sampleUnit1End.setCode("END")

        def sampleUnit2BronEnd = new Unit()
        sampleUnit2BronEnd.setAmount(sampleAmount)
        sampleUnit2BronEnd.setNAV(sampleNav)
        sampleUnit2BronEnd.setISIN(sampleIsin2)
        sampleUnit2BronEnd.setCurrency(sampleCurrency)
        sampleUnit2BronEnd.setCode("END")
        sampleUnit2BronEnd.setAdditionalFeature("BRON")

        def sampleUnit2End = new Unit()
        sampleUnit2End.setAmount(sampleAmount)
        sampleUnit2End.setNAV(sampleNav)
        sampleUnit2End.setISIN(sampleIsin2)
        sampleUnit2End.setCurrency(sampleCurrency)
        sampleUnit2End.setCode("END")

        def sampleUnit3End = new Unit()
        sampleUnit3End.setAmount(sampleAmount)
        sampleUnit3End.setNAV(sampleNav)
        sampleUnit3End.setISIN(sampleIsin3)
        sampleUnit3End.setCurrency(sampleCurrency)
        sampleUnit3End.setCode("END")

        def sampleUnit4Ufr = new Unit()
        sampleUnit4Ufr.setAmount(sampleAmount)
        sampleUnit4Ufr.setISIN(sampleIsin4)
        sampleUnit4Ufr.setNAV(null)
        sampleUnit4Ufr.setCode("UFR")
        sampleUnit4Ufr.setComment("Osakute vahetus")
        sampleUnit4Ufr.setCurrency(sampleCurrency)

        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [sampleUnit1Begin, sampleUnit1End, sampleUnit2BronEnd, sampleUnit2End, sampleUnit3End, sampleUnit4Ufr]
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
