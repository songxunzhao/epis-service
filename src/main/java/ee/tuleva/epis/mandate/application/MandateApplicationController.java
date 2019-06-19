package ee.tuleva.epis.mandate.application;

import ee.tuleva.epis.config.UserPrincipal;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MandateApplicationController {

    private final MandateApplicationListService mandateApplicationListService;

    @ApiOperation(value = "Get list of mandate exchange applications")
    @RequestMapping(method = GET, value = "/exchanges")
    public List<MandateExchangeApplicationResponse> get(@ApiIgnore @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Getting mandate applications for {}", principal.getPersonalCode());
        return mandateApplicationListService.get(principal.getPersonalCode());
    }

}
