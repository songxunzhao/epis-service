package ee.tuleva.epis.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowStatement {

    @Builder.Default
    private Map<String, Transaction> startBalance = new HashMap<>();
    @Builder.Default
    private Map<String, Transaction> endBalance = new HashMap<>();
    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    public void putStartBalance(String isin, Transaction transaction) {
        startBalance.put(isin, transaction);
    }

    public void putEndBalance(String isin, Transaction transaction) {
        endBalance.put(isin, transaction);
    }
}
