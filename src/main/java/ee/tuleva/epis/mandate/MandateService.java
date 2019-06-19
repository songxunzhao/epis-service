package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.UserPrincipal;

import java.util.List;

public interface MandateService {

    List<MandateResponse> sendMandate(UserPrincipal principal, MandateCommand mandateCommand);

    boolean supports(Integer pillar);
}
