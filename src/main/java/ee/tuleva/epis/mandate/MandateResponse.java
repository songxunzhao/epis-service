package ee.tuleva.epis.mandate;

import ee.tuleva.epis.mandate.application.MandateApplicationType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandateResponse {
    private boolean successful;
    private Integer errorCode;
    private String errorMessage;
    private MandateApplicationType applicationType;
    private String processId;
}
