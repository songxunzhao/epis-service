package ee.tuleva.epis.contact;

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
public class ContactDetailsController {

    private final ContactDetailsService contactDetailsService;

    @ApiOperation(value = "Get contact details")
    @RequestMapping(method = GET, value = "/contact-details")
    public ContactDetails getContactDetails(@ApiIgnore @AuthenticationPrincipal String personalCode) {
        return contactDetailsService.get(personalCode);
    }

}
