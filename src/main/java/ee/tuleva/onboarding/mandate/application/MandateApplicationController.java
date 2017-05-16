package ee.tuleva.onboarding.mandate.application;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MandateApplicationController {

    private final MandateApplicationListService mandateApplicationListService;

    @ApiOperation(value = "Get list of mandate applications")
    @RequestMapping(method = GET, value = "/mandate-application")
    public void get() throws InterruptedException {
        mandateApplicationListService.get("38812022762");

    }

}
