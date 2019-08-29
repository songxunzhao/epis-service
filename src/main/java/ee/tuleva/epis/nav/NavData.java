package ee.tuleva.epis.nav;

import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
public class NavData {
    String isin;
    LocalDate date;
    BigDecimal value;
}
