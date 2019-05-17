package ee.tuleva.epis.mandate;

import java.util.List;

public interface MandateService {

    List<MandateResponse> sendMandate(String personalCode, MandateCommand mandateCommand);

    boolean supports(Integer pillar);
}
