package ee.tuleva.epis.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserPrincipal {
    private String personalCode;
    private String firstName;
    private String lastName;
}
