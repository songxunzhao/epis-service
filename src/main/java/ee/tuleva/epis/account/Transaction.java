package ee.tuleva.epis.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@Data
public class Transaction {
    private static final String DEFAULT_CURRENCY = "EUR";
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("0.001");

    private Instant time;
    private BigDecimal amount;
    private String currency;
    private Integer pillar;

    public static class TransactionBuilder {
        public TransactionBuilder replaceNulls() {
            if (this.amount == null || this.amount.abs().compareTo(MIN_AMOUNT) < 0) {
                this.amount = BigDecimal.ZERO;
            }
            if (this.currency == null && this.amount.compareTo(BigDecimal.ZERO) == 0) {
                this.currency = DEFAULT_CURRENCY;
            }
            return this;
        }
    }
}
