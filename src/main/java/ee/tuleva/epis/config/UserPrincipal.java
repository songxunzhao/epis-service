package ee.tuleva.epis.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPrincipal {
    private String personalCode;
    private String firstName;
    private String lastName;
}
