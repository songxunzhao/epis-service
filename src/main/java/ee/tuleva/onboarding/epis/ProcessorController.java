package ee.tuleva.onboarding.epis;

import ee.tuleva.onboarding.epis.command.CreateProcessingCommand;
import ee.tuleva.onboarding.mandate.processor.implementation.MhubProcessRunner;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

// but ugly proof of concept
@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ProcessorController {

    private final MhubProcessRunner mhubProcessRunner;

    @ApiOperation(value = "Create mandate processing")
    @RequestMapping(method = POST, value = "/processing")
    public CreateProcessingCommand create(@Valid @RequestBody CreateProcessingCommand createProcessingCommand) {
        mhubProcessRunner.process(createProcessingCommand.getMessages());

        return createProcessingCommand;
    }
}
