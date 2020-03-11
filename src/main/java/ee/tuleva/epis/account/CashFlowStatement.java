package ee.tuleva.epis.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.*;

import static java.math.BigDecimal.ZERO;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowStatement {

    @Builder.Default
    private Map<String, Transaction> startBalance = new TreeMap<>();
    @Builder.Default
    private Map<String, Transaction> endBalance = new TreeMap<>();
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    public void addStartBalance(String isin, Transaction transaction) {
        add(isin, transaction, startBalance);
    }

    public void addEndBalance(String isin, Transaction transaction) {
        add(isin, transaction, endBalance);
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    private void add(String isin, Transaction transaction, Map<String, Transaction> balance) {
        if (balance.containsKey(isin)) {
            Transaction existingTransaction = balance.get(isin);
            Transaction newTransaction = Transaction.builder()
                .date(existingTransaction.getDate())
                .isin(existingTransaction.getIsin())
                .amount(add(existingTransaction.getAmount(), transaction.getAmount()))
                .units(add(existingTransaction.getUnits(), transaction.getUnits()))
                .type(existingTransaction.getType())
                .build();
            balance.put(isin, newTransaction);
        } else {
            balance.put(isin, transaction);
        }
    }

    private BigDecimal add(BigDecimal base, BigDecimal augend) {
        if (base == null) {
            base = ZERO;
        }
        if (augend == null) {
            augend = ZERO;
        }
        return base.add(augend);
    }

    public CashFlowStatement sort() {
        Collections.sort(transactions);
        return this;
    }
}
