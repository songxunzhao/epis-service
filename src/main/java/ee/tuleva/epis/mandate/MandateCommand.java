package ee.tuleva.epis.mandate;

import ee.tuleva.epis.mandate.application.FundTransferExchange;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
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

}
