package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.UserPrincipal;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MandateController {

    private final MandateService mandateService;

    @ApiOperation(value = "Create mandate")
    @PostMapping("/mandates")
    public CreateMandateResponseDto create(@Valid @RequestBody MandateCommand mandateCommand,
                                        @ApiIgnore @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Sending mandate for person: mandate={}, person={}", mandateCommand, principal.getPersonalCode());
        List<MandateResponse> mandateResponses = mandateService.sendMandate(principal, mandateCommand);
        return new CreateMandateResponseDto(mandateResponses);
    }

    @Data
    @AllArgsConstructor
    private static class CreateMandateResponseDto {
        private List<MandateResponse> mandateResponses;
    }
}
