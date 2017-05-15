package ee.tuleva.onboarding.mandate.application;

import ee.tuleva.onboarding.epis.EpisMessage;
import ee.tuleva.onboarding.epis.EpisMessageService;
import ee.tuleva.onboarding.epis.EpisMessageType;
import ee.tuleva.onboarding.epis.EpisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListService {

    private final EpisService episService;
    private final EpisMessageService episMessageService;
    private final MandateApplicationListMessageCreatorService mandateApplicationListMessageCreatorService;

    public List<MandateApplicationResponse> get(String personalCode) {

        EpisMessage episMessage = episMessageService.get(
                EpisMessageType.LIST_APPLICATIONS,
                mandateApplicationListMessageCreatorService.getMessage(personalCode)
        );

        episService.send(episMessage.getContent());

        return null;
    }

}
