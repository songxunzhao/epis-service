package ee.tuleva.onboarding.mandate.application;

import ee.tuleva.onboarding.mandate.MandateApplicationType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class MandateApplicationResponse {

    private MandateApplicationType type; //ApplicationType
    private String currency;
    private String date; //DocumentDate
    private Long id; //DocumentId
    private Long documentNumber; //DocumentNumber
    private BigDecimal acmount; //PaymentAmount
    private String status; // TODO
    private List<FundTransferExchange> fundTransferExchanges;

}
