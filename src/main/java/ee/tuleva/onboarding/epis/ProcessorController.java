package ee.tuleva.onboarding.epis;

import ee.tuleva.onboarding.epis.command.CreateProcessing;
import ee.tuleva.onboarding.error.ValidationErrorsException;
import ee.tuleva.onboarding.mandate.processor.implementation.MhubProcessRunner;
import ee.tuleva.onboarding.user.User;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

// but ugly proof of concept
@Slf4j
@RestController
@RequestMapping("/v0")
@RequiredArgsConstructor
public class ProcessorController {

    private final MhubProcessRunner mhubProcessRunner;

    @ApiOperation(value = "Create mandate processing")
    @RequestMapping(method = POST, value = "/processing")
    public CreateProcessing create(@ApiIgnore @AuthenticationPrincipal User user,
                                   @Valid @RequestBody CreateProcessing createProcessing,
                                   @ApiIgnore @Valid Errors errors) throws ValidationErrorsException {
//        if (errors.hasErrors()) {
//            log.info("Create mandate command is not valid: {}", errors);
//            throw new ValidationErrorsException(errors);
//        }

        mhubProcessRunner.process(createProcessing.getMessages());

        return createProcessing;
    }
}
