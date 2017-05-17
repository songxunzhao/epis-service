package ee.tuleva.onboarding.mandate.application;

import ee.tuleva.onboarding.epis.*;
import ee.tuleva.onboarding.epis.request.EpisMessage;
import ee.tuleva.onboarding.epis.request.EpisMessageService;
import ee.tuleva.onboarding.epis.response.EpisMessageResponseStore;
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
    private final EpisMessageResponseStore episMessageResponseStore;

    public List<MandateApplicationResponse> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);
        episMessageResponseStore.pop(message.getId());

        return null;//(List<MandateApplicationResponse>);
    }

    private EpisMessage sendQuery(String personalCode) {
        EpisMessage episMessage = episMessageService.get(
                EpisMessageType.LIST_APPLICATIONS,
                mandateApplicationListMessageCreatorService.getMessage(personalCode)
        );

        episService.send(episMessage.getContent());

        return episMessage;
    }

}
