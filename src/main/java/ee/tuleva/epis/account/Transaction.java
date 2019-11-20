package ee.tuleva.epis.account;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Builder
@Data
public class Transaction {
    private String isin;
    private LocalDate date;
    private BigDecimal units;
    private BigDecimal amount;
    private final String currency = "EUR";
    private final Type type;

    public enum Type {
        CONTRIBUTION;

        public static Type from(Integer purposeCode) {
            // 1 = Osakute väljalase laekumiste alusel (2. sammas)
            // 41 = Osakute väljalase tööandjalt laekumiste alusel (3. sammas)
            // 42 = Osakute väljalase isikult laekumiste alusel (3. sammas)
            if (Arrays.asList(1, 41, 42).contains(purposeCode)) {
                return CONTRIBUTION;
            }
            return null; // We don't care about other types for now
        }
    }
}
