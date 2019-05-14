package ee.tuleva.epis.account;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
public class Transaction {
  private Instant time;
  private BigDecimal amount;
  private String currency;

  @Getter(AccessLevel.NONE)
  private final String DEFAULT_CURRENCY = "EUR";
  @Getter(AccessLevel.NONE)
  private final BigDecimal MIN_AMOUNT = BigDecimal.valueOf(0.001);

  public Transaction replaceNulls() {
    if (this.getAmount() == null || this.getAmount().abs().compareTo(MIN_AMOUNT) < 0) {
      this.setAmount(BigDecimal.ZERO);
    }
    if (this.getCurrency() == null && this.getAmount().compareTo(BigDecimal.ZERO) == 0) {
      this.setCurrency(DEFAULT_CURRENCY);
    }
    return this;
  }
}
