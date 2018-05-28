package ee.tuleva.epis.account;

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
public class AccountStatementController {

    private final AccountStatementService accountStatementService;

    @ApiOperation(value = "Get personal data")
    @RequestMapping(method = GET, value = "/account-statement")
    public List<FundBalance> get(@ApiIgnore @AuthenticationPrincipal String personalCode) {
        log.info("Getting account statement for {}", personalCode);
        return accountStatementService.get(personalCode);
    }

}
