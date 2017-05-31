package ee.tuleva.epis.mandate.application;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FundTransferExchange {

    private BigDecimal amount;
    private String sourceFundIsin;
    private String targetFundIsin;

}
