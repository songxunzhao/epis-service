package ee.tuleva.epis.mandate.processor.implementation;

import ee.tuleva.epis.mandate.application.MandateApplicationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MandateXmlMessage {
    private String processId;
    private String message;
    private MandateApplicationType type;

}
