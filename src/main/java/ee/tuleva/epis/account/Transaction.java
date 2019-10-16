package ee.tuleva.epis.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
public class Transaction {
    private String isin;
    private LocalDate date;
    private BigDecimal units;
    private BigDecimal amount;
    private final String currency = "EUR";
}
