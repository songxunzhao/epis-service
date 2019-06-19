package ee.tuleva.epis.contact;

import ee.tuleva.epis.config.UserPrincipal;
import io.swagger.annotations.ApiOperation;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ContactDetailsController {

    private final ContactDetailsService contactDetailsService;

    @ApiOperation(value = "Get contact details")
    @GetMapping("/contact-details")
    public ContactDetails getContactDetails(@ApiIgnore @AuthenticationPrincipal UserPrincipal principal) {
        ContactDetails contactDetails = contactDetailsService.getContactDetails(principal.getPersonalCode());
        log.info("Returning contact details for {}: {}", principal.getPersonalCode(), contactDetails);
        return contactDetails;
    }

    @ApiOperation(value = "Update contact details")
    @PostMapping("/contact-details")
    public ContactDetails updateContactDetails(@Valid @RequestBody ContactDetails contactDetails,
                                               @ApiIgnore @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Updating contact details for {}: {}", principal.getPersonalCode(), contactDetails);
        contactDetailsService.updateContactDetails(principal.getPersonalCode(), contactDetails);
        return contactDetailsService.getContactDetails(principal.getPersonalCode());
    }

}
