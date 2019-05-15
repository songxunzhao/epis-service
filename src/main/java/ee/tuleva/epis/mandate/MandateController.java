package ee.tuleva.epis.mandate;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MandateController {

    private final MandateService mandateService;

    @ApiOperation(value = "Create mandate")
    @PostMapping("/mandates")
    public MandateResponse create(
        @Valid @RequestBody MandateCommand mandateCommand
    ) {
        return mandateService.sendMandate(mandateCommand);
    }
}
