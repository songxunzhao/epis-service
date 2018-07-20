package ee.tuleva.epis.account;

import lombok.*;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
@Slf4j
public class Transaction {
  private Instant time;
  private BigDecimal amount;
  private String currency;

  private final String DEFAULT_CURRENCY = "EUR";
  private final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.001);

  public Transaction replaceNulls() {
    if (this.getAmount() == null || this.getAmount().compareTo(MIN_AMOUNT) < 0) {
      this.setAmount(BigDecimal.ZERO);
    }
    if (this.getCurrency() == null && this.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      this.setCurrency(DEFAULT_CURRENCY);
    }
    return this;
  }
}
