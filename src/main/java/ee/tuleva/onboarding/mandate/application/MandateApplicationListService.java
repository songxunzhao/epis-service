package ee.tuleva.onboarding.mandate.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.tuleva.onboarding.epis.EpisMessageType;
import ee.tuleva.onboarding.epis.EpisService;
import ee.tuleva.onboarding.epis.request.EpisMessage;
import ee.tuleva.onboarding.epis.request.EpisMessageService;
import ee.tuleva.onboarding.epis.response.EpisMessageResponseStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListService {

    private final EpisService episService;
    private final EpisMessageService episMessageService;
    private final MandateApplicationListMessageCreatorService mandateApplicationListMessageCreatorService;
    private final EpisMessageResponseStore episMessageResponseStore;

    public List<MandateExchangeApplicationResponse> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);

        String applicationListJson = (String) episMessageResponseStore.pop(message.getId());

        List<MandateExchangeApplicationResponse> applications = null;
        try {
            applications =
                    (new ObjectMapper()).readValue(applicationListJson,
                            List.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return applications;
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
