package ee.tuleva.epis.epis.command;

import ee.tuleva.epis.mandate.processor.implementation.MandateXmlMessage;
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
