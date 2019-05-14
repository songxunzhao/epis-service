package ee.tuleva.epis.account;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class AccountStatementController {

    private final AccountStatementService accountStatementService;

    @ApiOperation(value = "Get account statement")
    @RequestMapping(method = GET, value = "/account-statement")
    public List<FundBalance> get(@ApiIgnore @AuthenticationPrincipal String personalCode) {
        log.info("Getting account statement for {}", personalCode);
        return accountStatementService.getAccountStatement(personalCode);
    }

    @ApiOperation(value = "Get account transactions by start and end dates")
    @RequestMapping(method = GET, value = "/account-cash-flow-statement")
    public CashFlowStatement getCashFlowStatementByPeriod(@ApiIgnore @AuthenticationPrincipal String personalCode,
                                                          @RequestParam("from-date") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate fromDate,
                                                          @RequestParam("to-date") @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate toDate) {
        log.info("Getting account statement for {} from {} to {}", personalCode, fromDate, toDate);

        return accountStatementService.getCashFlowStatement(personalCode, fromDate, toDate);
    }

}
