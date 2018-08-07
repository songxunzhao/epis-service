package ee.tuleva.epis.epis.converter

import ee.tuleva.epis.account.Transaction
import ee.tuleva.epis.account.CashFlowStatement
import ee.tuleva.epis.epis.exception.EpisMessageException
import ee.x_road.epis.producer.AnswerType
import ee.x_road.epis.producer.EpisX14ResponseType
import ee.x_road.epis.producer.EpisX14Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import java.time.Instant
import java.time.temporal.ChronoUnit

class EpisX14TypeToCashFlowStatementConverterSpec extends Specification {

    def converter = new EpisX14TypeToCashFlowStatementConverter()

    BigDecimal samplePrice = new BigDecimal("2.4")
    BigDecimal sampleNAV = new BigDecimal("0.64")
    BigDecimal sampleAmount = new BigDecimal("101.12")
    Instant sampleTime = Instant.now().minus(1, ChronoUnit.DAYS);
    Instant sampleTime2 = Instant.now().minus(7, ChronoUnit.DAYS);
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"
    String sampleCurrency = "EUR"

    def "converts OK epis response"() {
        when:
        CashFlowStatement response = converter.convert(getSampleSource())
        Transaction start = response.getStartBalance().get(sampleIsin1)
        Transaction end = response.getEndBalance().get(sampleIsin1)
        List<Transaction> transactions = response.getTransactions()

        then:
        start.time == sampleTime
        start.amount == sampleNAV * sampleAmount
        start.currency == sampleCurrency

        end.time == sampleTime2
        end.amount == sampleNAV * sampleAmount
        end.currency == sampleCurrency

        transactions.size() == 2

        transactions.first().time == sampleTime
        transactions.first().amount == sampleAmount
        transactions.first().currency == sampleCurrency

        transactions.get(1).time == sampleTime2
        transactions.get(1).amount == sampleAmount
        transactions.get(1).currency == sampleCurrency
    }


    def "avoids nulls in amounts and currency values"() {
        when:
        CashFlowStatement response = converter.convert(getSampleSourceWithNulls())
        Transaction start = response.getStartBalance().get(sampleIsin1)
        Transaction end = response.getEndBalance().get(sampleIsin1)
        List<Transaction> transactions = response.getTransactions()

        then:
        transactions.size() == 1

        start.amount.compareTo(BigDecimal.ZERO) > 0
        start.currency == null

        end.amount.compareTo(BigDecimal.ZERO) == 0
        end.currency == "EUR"

        transactions.first().amount.compareTo(BigDecimal.ZERO) == 0
        transactions.first().currency == sampleCurrency
    }

    def "throws exception on NOK epis response"() {
        when:
        converter.convert(getSampleErrorResponse())

        then:
        thrown EpisMessageException
    }

    XMLGregorianCalendar instantToXMLGregorianCalendar(Instant time) {
        GregorianCalendar gCal = new GregorianCalendar();
        gCal.setTimeInMillis(time.toEpochMilli());
        XMLGregorianCalendar xmlTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
    }

    EpisX14ResponseType.Unit getSampleUnit(Instant transactionDate, String code, String isin, BigDecimal nav, String currency) {
        def sampleUnit = new EpisX14ResponseType.Unit()
        sampleUnit.setTransactionDate(instantToXMLGregorianCalendar(transactionDate))
        sampleUnit.setCode(code)
        sampleUnit.setISIN(isin)
        sampleUnit.setNAV(nav)
        sampleUnit.setAmount(sampleAmount)
        sampleUnit.setPrice(samplePrice)
        sampleUnit.setCurrency(currency)
        return sampleUnit
    }

    EpisX14ResponseType.Cash getSampleCash(Instant transactionDate, String code, BigDecimal amount, String currency) {
        def sampleCash = new EpisX14ResponseType.Cash()
        sampleCash.setTransactionDate(instantToXMLGregorianCalendar(transactionDate))
        sampleCash.setCode(code)
        sampleCash.setAmount(amount)
        sampleCash.setCurrency(currency)
        return sampleCash;
    }

    EpisX14Type getSampleSourceWithNulls() {
        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [
                    getSampleUnit(sampleTime, 'BEGIN', sampleIsin1, 10, null),
                    getSampleUnit(sampleTime, 'END', sampleIsin1, 0.0000001, null)
            ]
            getCash() >> [
                    getSampleCash(Instant.now(), 'RIF', 0.000000001, null)

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
                    getSampleUnit(sampleTime2, 'END', sampleIsin1, sampleNAV, sampleCurrency)
            ]
            getCash() >> [
                    getSampleCash(Instant.now(), 'NO_RIF', sampleAmount, sampleCurrency),
                    getSampleCash(sampleTime, 'RIF', sampleAmount, sampleCurrency),
                    getSampleCash(sampleTime2, 'RIF', sampleAmount, sampleCurrency),
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