package ee.tuleva.epis.mandate.application;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Builder
public class FundTransferExchange {

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String sourceFundIsin;

    @NotNull
    private String targetFundIsin;

    @NotNull
    private String processId;

}
