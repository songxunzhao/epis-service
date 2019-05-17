package ee.tuleva.epis.mandate;

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
    public List<MandateResponse> sendMandate(String personalCode, MandateCommand mandateCommand) {
        for (MandateService mandateService : mandateServices) {
            if (mandateService.supports(mandateCommand.getPillar())) {
                return mandateService.sendMandate(personalCode, mandateCommand);
            }
        }
        throw new IllegalStateException("Unsupported pillar" + mandateCommand.getPillar());
    }

    @Override
    public boolean supports(Integer pillar) {
        for (MandateService mandateService : mandateServices) {
            if (mandateService.supports(pillar)) {
                return true;
            }
        }
        return false;
    }
}
