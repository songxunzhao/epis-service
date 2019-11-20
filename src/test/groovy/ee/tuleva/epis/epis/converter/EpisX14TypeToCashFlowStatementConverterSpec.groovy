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
import java.time.LocalDate

import static ee.tuleva.epis.account.Transaction.Type.CONTRIBUTION
import static ee.x_road.epis.producer.EpisX14ResponseType.Unit
import static java.math.RoundingMode.HALF_UP

class EpisX14TypeToCashFlowStatementConverterSpec extends Specification {

    def resultValidator = new EpisResultValidator()

    def converter = new EpisX14TypeToCashFlowStatementConverter(resultValidator)

    LocalDate sampleTime1 = LocalDate.parse("2019-05-13")
    LocalDate sampleTime2 = LocalDate.parse("2019-05-01")
    LocalDate sampleTime3 = LocalDate.parse("2019-04-11")
    LocalDate sampleTime4 = LocalDate.parse("2019-04-15")
    String sampleIsin1 = "sampleIsin1"
    String sampleIsin2 = "sampleIsin2"

    EpisX14Type getSampleSource() {
        def result = new ResultType()
        result.result = AnswerType.OK

        def episX14ResponseType = Mock(EpisX14ResponseType, {
            getUnit() >> [
                unit(sampleTime1, 'BEGIN', sampleIsin1, 'EEK', 0.0, null, 0.65),
                purpose(1, unit(sampleTime1, 'OVI', sampleIsin1, 'EEK', 15.6466, 10.0, 10.1)),
                purpose(41, unit(sampleTime2, 'OVI', sampleIsin1, 'EEK', 15.6466, 2.0, 2.1)),
                unit(sampleTime2, 'OVI', sampleIsin1, 'EEK', null, 2.0, 2.1),
                unit(sampleTime3, 'END', sampleIsin1, 'EUR', 1.5, null, 12.0),
                unit(sampleTime1, 'BEGIN', sampleIsin2, 'EUR', 1.5, null, 10.0),
                purpose(42, unit(sampleTime2, 'OVI', sampleIsin2, 'EUR', 100.0, 10.0, 11.0)),
                unit(sampleTime3, 'OVF', sampleIsin2, 'EUR', -70.1, 8.22, 9.0),
                bron(unit(sampleTime3, 'OVF', sampleIsin2, 'EUR', -70.1, 8.22, 9.0)),
                unit(sampleTime3, 'OVF', sampleIsin2, null, 1.0, 1.0, 1.0),
                unit(sampleTime3, 'OVF', sampleIsin2, 'EUR', 1.0, null, null),
                unit(sampleTime3, 'UFR', sampleIsin2, 'EUR', -1000.0, 1.0, 1.0),
                bron(unit(sampleTime3, 'UFR', sampleIsin2, 'EUR', 1000.0, 1.0, 1.0)),
                bron(unit(sampleTime3, 'UFU', sampleIsin2, 'EUR', -1000.0, 1.0, 1.0)),
                unit(sampleTime3, 'UFU', sampleIsin2, 'EUR', 1000.0, 1.0, 1.0),
                unit(sampleTime3, 'UFR', sampleIsin2, 'EUR', -500.0, 1.0, 1.0),
                bron(unit(sampleTime3, 'UFR', sampleIsin2, 'EUR', 500.0, 1.0, 1.0)),
                bron(unit(sampleTime4, 'UUF', sampleIsin2, 'EUR', -500.0, 1.0, 1.0)),
                unit(sampleTime3, 'END', sampleIsin2, 'EUR', 50.0, null, 8.0),
                bron(unit(sampleTime3, 'END', sampleIsin2, 'EUR', 100.0, null, 9.0)),
            ]
            getResults() >> result
        })

        def source = new EpisX14Type()
        source.setResponse(episX14ResponseType)

        return source
    }

    Unit bron(Unit unit) {
        unit.setAdditionalFeature("BRON")
        return unit
    }

    Unit purpose(Integer purposeCode, Unit unit) {
        unit.setPurposeCode(purposeCode)
        return unit
    }

    def "converts OK epis response"() {
        when:
        CashFlowStatement cashFlow = converter.convert(getSampleSource())
        List<Transaction> transactions = cashFlow.getTransactions()

        then:
        with(cashFlow.getStartBalance().get(sampleIsin1)) {
            date == sampleTime1
            amount == 0.0
            currency == 'EUR'
        }

        with(cashFlow.getEndBalance().get(sampleIsin1)) {
            date == sampleTime3
            amount == 1.5 * 12.0
            currency == 'EUR'
        }

        with(cashFlow.getStartBalance().get(sampleIsin2)) {
            date == sampleTime1
            amount == 1.5 * 10.0
            currency == 'EUR'
        }

        with(cashFlow.getEndBalance().get(sampleIsin2)) {
            date == sampleTime3
            amount == (50.0 * 8.0) + (100.0 * 9.0)
            currency == 'EUR'
        }

        transactions.size() == 6

        with(transactions.get(0)) {
            date == sampleTime1
            units == 15.6466
            amount == 15.6466 * 10.0 / 15.6466
            currency == 'EUR'
            isin == sampleIsin1
            type == CONTRIBUTION
        }
        with(transactions.get(1)) {
            date == sampleTime2
            units == 15.6466
            amount == 15.6466 * 2.0 / 15.6466
            currency == 'EUR'
            isin == sampleIsin1
            type == CONTRIBUTION
        }
        with(transactions.get(2)) {
            date == sampleTime2
            units == 100.0
            amount == 100.0 * 10.0
            currency == 'EUR'
            isin == sampleIsin2
            type == CONTRIBUTION
        }
        with(transactions.get(3)) {
            date == sampleTime3
            units == -70.1
            amount == (-70.1 * 8.22).setScale(2, HALF_UP)
            currency == 'EUR'
            isin == sampleIsin2
            type == null
        }
        with(transactions.get(4)) {
            date == sampleTime3
            units == 1.0
            amount == 0.0
            currency == 'EUR'
            isin == sampleIsin2
            type == null
        }
        with(transactions.get(5)) {
            date == sampleTime4
            units == -500.0
            amount == -500.0
            currency == 'EUR'
            isin == sampleIsin2
            type == null
        }
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

    Unit unit(LocalDate transactionDate, String code, String isin, String currency, BigDecimal amount,
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
