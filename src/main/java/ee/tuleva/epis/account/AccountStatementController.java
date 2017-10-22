package ee.tuleva.epis.account;

import ee.x_road.epis.producer.EpisX14Type;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AccountStatementController {

    private final AccountStatementService accountStatementService;

    @ApiOperation(value = "Get personal data")
    @RequestMapping(method = GET, value = "/account-statement")
    public EpisX14Type get(@ApiIgnore @AuthenticationPrincipal String personalCode) {
        return accountStatementService.get(personalCode);
    }

}
