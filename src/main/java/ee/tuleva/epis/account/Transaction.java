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
}
