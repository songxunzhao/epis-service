package ee.tuleva.epis.account

import spock.lang.Specification

import java.time.LocalDate

class CashFlowStatementSpec extends Specification {

    def "addEndBalance"() {
        given:
        def isin = "EE123"
        def cashFlowStatement = new CashFlowStatement()
        def transaction = Transaction.builder()
            .date(LocalDate.now())
            .amount(1.0)
            .units((2.0))
            .build()

        when:
        cashFlowStatement.addEndBalance(isin, transaction)
        cashFlowStatement.addEndBalance(isin, transaction)

        then:
        cashFlowStatement.endBalance[isin].amount == 2 * transaction.amount
        cashFlowStatement.endBalance[isin].units == 2 * transaction.units
    }
}
