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

    def "sorts transactions first by isin and then by date"() {
        given:
        def transactions = [
            Transaction.builder().isin("EE123").date(LocalDate.parse("2020-01-07")).build(),
            Transaction.builder().isin("EE234").date(LocalDate.parse("2020-01-06")).build(),
            Transaction.builder().isin("EE123").date(LocalDate.parse("2020-01-05")).build(),
            Transaction.builder().isin("EE234").date(LocalDate.parse("2020-01-04")).build(),
            Transaction.builder().isin("EE123").date(LocalDate.parse("2020-01-03")).build(),
            Transaction.builder().isin("EE234").date(LocalDate.parse("2020-01-02")).build(),
        ]
        def cashFlowStatement = new CashFlowStatement()
        cashFlowStatement.setTransactions(transactions)

        when:
        cashFlowStatement.sort()

        then:
        // In Groovy, the equals operator == translates to a.compareTo(b) == 0, if they are Comparable,
        // so it won't detect bugs.
        Objects.equals(transactions, [
            Transaction.builder().isin("EE123").date(LocalDate.parse("2020-01-03")).build(),
            Transaction.builder().isin("EE123").date(LocalDate.parse("2020-01-05")).build(),
            Transaction.builder().isin("EE123").date(LocalDate.parse("2020-01-07")).build(),
            Transaction.builder().isin("EE234").date(LocalDate.parse("2020-01-02")).build(),
            Transaction.builder().isin("EE234").date(LocalDate.parse("2020-01-04")).build(),
            Transaction.builder().isin("EE234").date(LocalDate.parse("2020-01-06")).build(),
        ])
    }
}
