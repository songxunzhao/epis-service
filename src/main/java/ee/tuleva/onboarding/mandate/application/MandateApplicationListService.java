package ee.tuleva.onboarding.mandate.application;

import com.fasterxml.jackson.core.type.TypeReference;
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

    public List<MandateApplicationResponse> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);

        String applicationListJson = (String) episMessageResponseStore.pop(message.getId());


        try {
            List<MandateApplicationResponse> applications =
                    (new ObjectMapper()).readValue(applicationListJson,
                            new TypeReference<List<MandateApplicationResponse>>(){});

            log.info(applications.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }


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
