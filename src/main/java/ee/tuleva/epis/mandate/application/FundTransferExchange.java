package ee.tuleva.epis.mandate.application;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
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
