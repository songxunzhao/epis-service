package ee.tuleva.epis.account;


import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
public class FundBalance {
    private String isin;
    private BigDecimal value;
    private String currency;
    private BigDecimal units;
    private BigDecimal nav;
    private Integer pillar;
    private boolean activeContributions;
}
