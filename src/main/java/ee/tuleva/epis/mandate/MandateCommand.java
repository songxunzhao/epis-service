package ee.tuleva.epis.mandate;

import ee.tuleva.epis.mandate.application.FundTransferExchange;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MandateCommand {

    @NotNull
    private Long id;

    @NotNull
    private String processId;

    private String futureContributionFundIsin;

    @NotNull
    private Instant createdDate;

    @NotNull
    private Integer pillar = 2;

    List<FundTransferExchange> fundTransferExchanges;

}
