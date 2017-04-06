package ee.tuleva.onboarding.mandate.content;

import ee.tuleva.onboarding.mandate.MandateApplicationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MandateXmlMessage {
    private String processId;
    private String message;
    private MandateApplicationType type;
}
