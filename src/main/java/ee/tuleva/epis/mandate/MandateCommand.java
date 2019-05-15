package ee.tuleva.epis.mandate;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class MandateCommand {
    @NotNull
    private Long id;

    @NotNull
    private String futureContributionFundIsin;

    @NotNull
    private Instant createdDate;

    private Integer pillar = 2;

    List<FundTransferExchange> fundTransferExchanges;

    @Getter
    @Setter
    public static class FundTransferExchange {
        private Long id;
        private String sourceFundIsin;
        private BigDecimal amount;
        private String targetFundIsin;
    }
}
