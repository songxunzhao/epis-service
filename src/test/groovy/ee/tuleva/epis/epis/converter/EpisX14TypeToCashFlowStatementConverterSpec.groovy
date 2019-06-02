package ee.tuleva.epis.epis.converter

import ee.tuleva.epis.account.CashFlowStatement
import ee.tuleva.epis.account.Transaction
import ee.tuleva.epis.epis.exception.EpisMessageException
import ee.tuleva.epis.epis.validator.EpisResultValidator
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import java.time.Instant

import static ee.x_road.epis.producer.EpisX14ResponseType.Cash
import static ee.x_road.epis.producer.EpisX14ResponseType.Unit

class EpisX14TypeToCashFlowStatementConverterSpec extends Specification {

    def resultValidator = new EpisResultValidator()

    def converter = new EpisX14TypeToCashFlowStatementConverter(resultValidator)

    BigDecimal samplePrice = new BigDecimal("2.4")
    BigDecimal sampleNAV = new BigDecimal("0.64")
    BigDecimal sampleAmount = new BigDecimal("101.12")
    BigDecimal sampleAmount2 = new BigDecimal("99.11")
    Instant sampleTime = Instant.parse("2019-05-13T22:13:27.141Z")
    Instant sampleTime2 = Instant.parse("2019-05-01T10:13:51.432Z")
    Instant sampleTime3 = Instant.parse("2019-04-11T15:16:11.754Z")
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"
    int samplePillar = 2
    int samplePillar2 = 3
    String sampleCurrency = "EUR"

    def "converts OK epis response"() {
        when:
        CashFlowStatement cashFlow = converter.convert(getSampleSource())
        Transaction start = cashFlow.getStartBalance().get(sampleIsin1)
        Transaction end = cashFlow.getEndBalance().get(sampleIsin1)
        List<Transaction> transactions = cashFlow.getTransactions()

        then:
        start.time == sampleTime
        start.amount == sampleNAV * sampleAmount
        start.currency == sampleCurrency
        start.pillar == null // unknown

        end.time == sampleTime2
        end.amount == sampleNAV * sampleAmount
        end.currency == sampleCurrency
        start.pillar == null // unknown

        transactions.size() == 3

        transactions.get(0).time == sampleTime
        transactions.get(0).amount == sampleAmount
        transactions.get(0).currency == sampleCurrency
        transactions.get(0).pillar == samplePillar

        transactions.get(1).time == sampleTime2
        transactions.get(1).amount == sampleAmount
        transactions.get(1).currency == sampleCurrency
        transactions.get(1).pillar == samplePillar

        transactions.get(2).time == sampleTime3
        transactions.get(2).amount == sampleAmount2
        transactions.get(2).currency == sampleCurrency
        transactions.get(2).pillar == samplePillar2
    }


    def "avoids nulls in amounts and currency values"() {
        when:
        CashFlowStatement response = converter.convert(getSampleSourceWithNulls())
        Transaction start = response.getStartBalance().get(sampleIsin1)
        Transaction end = response.getEndBalance().get(sampleIsin1)
        List<Transaction> transactions = response.getTransactions()

        then:
        transactions.size() == 2

        start.amount > BigDecimal.ZERO
        start.currency == null

        end.amount == BigDecimal.ZERO
        end.currency == "EUR"

        transactions.first().amount == BigDecimal.ZERO
        transactions.first().currency == sampleCurrency
        transactions.first().pillar == samplePillar
        transactions.last().amount == BigDecimal.ZERO
        transactions.last().currency == sampleCurrency
        transactions.last().pillar == samplePillar2
    }

    def "throws exception on NOK epis response"() {
        when:
        converter.convert(getSampleErrorResponse())

        then:
        thrown EpisMessageException
    }

    XMLGregorianCalendar instantToXMLGregorianCalendar(Instant time) {
        GregorianCalendar calendar = new GregorianCalendar()
        calendar.setTimeInMillis(time.toEpochMilli())
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar)
    }

    Unit getSampleUnit(Instant transactionDate, String code, String isin, BigDecimal nav, String currency) {
        def sampleUnit = new Unit()
        sampleUnit.setTransactionDate(instantToXMLGregorianCalendar(transactionDate))
        sampleUnit.setCode(code)
        sampleUnit.setISIN(isin)
        sampleUnit.setNAV(nav)
        sampleUnit.setAmount(sampleAmount)
        sampleUnit.setPrice(samplePrice)
        sampleUnit.setCurrency(currency)
        return sampleUnit
    }

    Cash getSampleCash(Instant transactionDate, String code, BigDecimal amount, String currency) {
        def sampleCash = new Cash()
        sampleCash.setTransactionDate(instantToXMLGregorianCalendar(transactionDate))
        sampleCash.setCode(code)
        sampleCash.setAmount(amount)
        sampleCash.setCurrency(currency)
        return sampleCash
    }

    EpisX14Type getSampleSourceWithNulls() {
        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [
                    getSampleUnit(sampleTime, 'BEGIN', sampleIsin1, 10.0, null),
                    getSampleUnit(sampleTime, 'END', sampleIsin1, 0.0000001, null)
            ]
            getCash() >> [
                    getSampleCash(Instant.now(), 'RIF', 0.000000001, null),
                    getSampleCash(Instant.now(), 'MIF', 0.000000001, null)
            ]
            getResults() >> result
        })

        def source = new EpisX14Type()
        source.setResponse(episX14ResponseType)

        return source
    }

    EpisX14Type getSampleSource() {
        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [
                    getSampleUnit(sampleTime, 'BEGIN', sampleIsin1, sampleNAV, sampleCurrency),
                    getSampleUnit(Instant.now(), 'OVI', sampleIsin1, sampleNAV, sampleCurrency),
                    getSampleUnit(sampleTime2, 'END', sampleIsin1, sampleNAV, sampleCurrency),
                    getSampleUnit(sampleTime, 'BEGIN', sampleIsin2, sampleNAV, sampleCurrency),
                    getSampleUnit(Instant.now(), 'OVI', sampleIsin2, sampleNAV, sampleCurrency),
                    getSampleUnit(sampleTime2, 'END', sampleIsin2, sampleNAV, sampleCurrency)
            ]
            getCash() >> [
                    getSampleCash(Instant.now(), 'NO_RIF', sampleAmount, sampleCurrency),
                    getSampleCash(sampleTime, 'RIF', sampleAmount, sampleCurrency),
                    getSampleCash(sampleTime2, 'RIF', sampleAmount, sampleCurrency),
                    getSampleCash(sampleTime3, 'MIF', sampleAmount2, sampleCurrency),
                    getSampleCash(Instant.now(), 'NO_RIF', sampleAmount, sampleCurrency)

            ]
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
