package ee.tuleva.onboarding.mandate.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListMessageCreatorService {

    public String getMessage(String personalCode) {

        String message = "<AVALDUSTE_LOETELU>\n" +
                "         <Request>\n" +
                "                       <PersonalData PersonId=\"" + personalCode + "\"/>\n" +
                "                                </Request>\n" +
                "      </AVALDUSTE_LOETELU>";

        return message;
    }

}
