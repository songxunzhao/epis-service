package ee.tuleva.epis.account;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

@Builder
@Data
public class Transaction implements Comparable<Transaction> {
    private String isin;
    private LocalDate date;
    private BigDecimal units;
    private BigDecimal amount;
    private final String currency = "EUR";
    private final Type type;

    public enum Type {
        CONTRIBUTION_CASH, CONTRIBUTION, SUBTRACTION, OTHER;

        @NonNull
        public static Type from(Integer purposeCode) {
            // 1 = Osakute väljalase laekumiste alusel (2. sammas)
            // 41 = Osakute väljalase tööandjalt laekumiste alusel (3. sammas)
            // 42 = Osakute väljalase isikult laekumiste alusel (3. sammas)
            if (Arrays.asList(1, 41, 42).contains(purposeCode)) {
                return CONTRIBUTION_CASH;
            }
            // 43 = Osakute väljalase kindlustuslepingust laekumiste alusel (3. sammas)
            if (Objects.equals(43, purposeCode)) {
                return CONTRIBUTION;
            }
            // 6 = Kindlustuslepingu sõlmimine (2. sammas)
            // 8 = Väljamaksed pensionifondist (2. sammas)
            // 9 = Täiendavad väljamaksed pensionifondist (2. sammas)
            // 10 = Pärimine vana (2. ja 3. sammas)
            // 16 = Täiendavad maksed kindlustusseltsi (2. sammas)
            // 18 = Pärandvara pankrott (2. sammas)
            // 19 = Ülekanne EU institutsioonide pensioniskeemi (2. sammas)
            // 21 = Osakute kustutamine - näiteks pärijate puudumisel peale 10 aastat (2. sammas)
            // 51 = Lunastamine (3. sammas)
            // 52 = Kohtutäituri lunastus  (3. sammas)
            // 53 = Kindlustusse ülekanne (3. sammas)
            // 55 = Pärimine uus (2. ja 3. sammas)
            // 56 = Ühisvara jagamine (3. sammas)
            if (Arrays.asList(6, 8, 9, 10, 16, 18, 19, 21, 51, 52, 53, 55, 56).contains(purposeCode)) {
                return SUBTRACTION;
            }
            return OTHER;
        }
    }

    @Override
    public int compareTo(@NonNull Transaction other) {
        int result = isin.compareToIgnoreCase(other.isin);
        if (result == 0) {
            return date.compareTo(other.date);
        }
        return result;
    }
}
