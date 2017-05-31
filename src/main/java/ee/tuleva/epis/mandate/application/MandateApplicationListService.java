package ee.tuleva.epis.mandate.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.tuleva.epis.epis.EpisMessageType;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageService;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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

        } catch (Exception e) {
            log.error(String.valueOf(e));
            applications = Arrays.asList();
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
