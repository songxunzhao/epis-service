package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
@AllArgsConstructor
public class CompositeMandateService implements MandateService {

    private List<MandateService> mandateServices;

    @Override
    public List<MandateResponse> sendMandate(UserPrincipal principal, MandateCommand mandateCommand) {
        return mandateServices.stream()
            .filter(mandateService -> mandateService.supports(mandateCommand.getPillar()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unsupported pillar: " + mandateCommand.getPillar()))
            .sendMandate(principal, mandateCommand);
    }

    @Override
    public boolean supports(Integer pillar) {
        return mandateServices.stream().anyMatch(mandateService -> mandateService.supports(pillar));
    }
}
