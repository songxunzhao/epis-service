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
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate

import static ee.x_road.epis.producer.EpisX14ResponseType.Cash
import static ee.x_road.epis.producer.EpisX14ResponseType.Unit
import static java.math.RoundingMode.HALF_UP

class EpisX14TypeToCashFlowStatementConverterSpec extends Specification {

    def resultValidator = new EpisResultValidator()

    def converter = new EpisX14TypeToCashFlowStatementConverter(resultValidator)

    LocalDate sampleTime1 = LocalDate.parse("2019-05-13")
    LocalDate sampleTime2 = LocalDate.parse("2019-05-01")
    LocalDate sampleTime3 = LocalDate.parse("2019-04-11")
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"

    EpisX14Type getSampleSource() {
        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [
                getSampleUnit(sampleTime1, 'BEGIN', sampleIsin1, 'EEK', 0.0, null, 0.65),
                getSampleUnit(sampleTime1, 'OVI', sampleIsin1, 'EEK', 15.6466, 10.0, 10.1),
                getSampleUnit(sampleTime2, 'OVI', sampleIsin1, 'EEK', 15.6466, 2.0, 2.1),
                getSampleUnit(sampleTime2, 'OVI', sampleIsin1, 'EEK', null, 2.0, 2.1),
                getSampleUnit(sampleTime3, 'END', sampleIsin1, 'EUR', 1.5, null, 12.0),
                getSampleUnit(sampleTime1, 'BEGIN', sampleIsin2, 'EUR', 1.5, null, 10.0),
                getSampleUnit(sampleTime2, 'OVI', sampleIsin2, 'EUR', 100.0, 10.0, 11.0),
                getSampleUnit(sampleTime3, 'OVF', sampleIsin2, 'EUR', -70.1, 8.22, 9.0),
                getSampleUnit(sampleTime3, 'OVF', sampleIsin2, null, 1.0, 1.0, 1.0),
                getSampleUnit(sampleTime3, 'END', sampleIsin2, 'EUR', 50.0, null, 8.0),
            ]
            getResults() >> result
        })

        def source = new EpisX14Type()
        source.setResponse(episX14ResponseType)

        return source
    }

    def "converts OK epis response"() {
        when:
        CashFlowStatement cashFlow = converter.convert(getSampleSource())
        Transaction start = cashFlow.getStartBalance().get(sampleIsin1)
        Transaction end = cashFlow.getEndBalance().get(sampleIsin1)
        List<Transaction> transactions = cashFlow.getTransactions()

        then:
        start.date == sampleTime1
        start.amount == 0.0
        start.currency == 'EUR'

        end.date == sampleTime3
        end.amount == 1.5 * 12.0
        end.currency == 'EUR'

        transactions.size() == 4

        transactions.get(0).date == sampleTime1
        transactions.get(0).amount == 15.6466 * 10.0 / 15.6466
        transactions.get(0).currency == 'EUR'
        transactions.get(0).isin == sampleIsin1

        transactions.get(1).date == sampleTime2
        transactions.get(1).amount == 15.6466 * 2.0 / 15.6466
        transactions.get(1).currency == 'EUR'
        transactions.get(1).isin == sampleIsin1

        transactions.get(2).date == sampleTime2
        transactions.get(2).amount == 100.0 * 10.0
        transactions.get(2).currency == 'EUR'
        transactions.get(2).isin == sampleIsin2

        transactions.get(3).date == sampleTime3
        transactions.get(3).amount == (-70.1 * 8.22).setScale(2, HALF_UP)
        transactions.get(3).currency == 'EUR'
        transactions.get(3).isin == sampleIsin2
    }

    def "throws exception on NOK epis response"() {
        when:
        converter.convert(getSampleErrorResponse())

        then:
        thrown EpisMessageException
    }

    XMLGregorianCalendar instantToXMLGregorianCalendar(LocalDate time) {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(time.toString())
    }

    Unit getSampleUnit(LocalDate transactionDate, String code, String isin, String currency, BigDecimal amount,
                       BigDecimal price, BigDecimal nav) {
        def sampleUnit = new Unit()
        sampleUnit.setTransactionDate(instantToXMLGregorianCalendar(transactionDate))
        sampleUnit.setCode(code)
        sampleUnit.setISIN(isin)
        sampleUnit.setAmount(amount)
        sampleUnit.setPrice(price)
        sampleUnit.setNAV(nav)
        sampleUnit.setCurrency(currency)
        return sampleUnit
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
