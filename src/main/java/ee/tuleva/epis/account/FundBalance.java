package ee.tuleva.epis.account;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class FundBalance {
    private String isin;
    private BigDecimal value;
    private String currency;
    private int pillar = 2;
    private boolean activeContributions = false;
}
