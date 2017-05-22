package ee.tuleva.onboarding.mandate.application;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
public class MandateExchangeApplicationResponse implements Serializable {

    private String currency;
    private Instant date;
    private String id;
    private String documentNumber;
    private BigDecimal amount;
    private MandateApplicationStatus status;
    private String sourceFundIsin;
    private String targetFundIsin;

    public Long getDate() {
        return this.date.getEpochSecond();
    }

}
