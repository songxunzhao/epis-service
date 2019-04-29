package ee.tuleva.epis.account;


import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
public class FundBalance {
    private String isin;
    private BigDecimal value;
    private String currency;
    @Builder.Default
    private int pillar = 2;
    @Builder.Default
    private boolean activeContributions = false;
}
