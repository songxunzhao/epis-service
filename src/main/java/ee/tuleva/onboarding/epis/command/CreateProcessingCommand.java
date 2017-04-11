package ee.tuleva.onboarding.epis.command;

import ee.tuleva.onboarding.mandate.processor.implementation.MandateXmlMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateProcessingCommand {

    List<MandateXmlMessage> messages;

}
