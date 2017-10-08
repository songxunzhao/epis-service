package ee.tuleva.epis.person;

import ee.x_road.epis.producer.EpisX12Type;
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
public class PersonController {

    private final PersonService personService;

    @ApiOperation(value = "Get personal data")
    @RequestMapping(method = GET, value = "/persons")
    public EpisX12Type get(@ApiIgnore @AuthenticationPrincipal String personalCode) {
        return personService.get(personalCode);
    }

}
