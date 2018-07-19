package ee.tuleva.epis.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowStatement {
  private Map<String, Transaction> startBalance = new HashMap<String, Transaction>();
  private Map<String, Transaction> endBalance = new HashMap<String, Transaction>();

  private List<Transaction> transactions;

  public void putStartBalance(String isin, Transaction transaction) { this.startBalance.put(isin, transaction); }

  public void putEndBalance(String isin, Transaction transaction) {
    this.endBalance.put(isin, transaction);
  }
}
