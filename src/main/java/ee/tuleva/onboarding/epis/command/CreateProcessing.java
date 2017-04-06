package ee.tuleva.onboarding.epis.command;

import ee.tuleva.onboarding.mandate.content.MandateXmlMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateProcessing {

    List<MandateXmlMessage> messages;

}
