package ee.tuleva.epis.mandate.application;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.epis.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.mandate.application.list.EpisApplicationListToMandateApplicationResponseListConverter;
import ee.x_road.epis.producer.EpisX26RequestType;
import ee.x_road.epis.producer.EpisX26ResponseType;
import ee.x_road.epis.producer.EpisX26Type;
import ee.x_road.epis.producer.PersonDataRequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final EpisApplicationListToMandateApplicationResponseListConverter converter;
    private final EpisMessageFactory episMessageFactory;

    public List<MandateExchangeApplicationResponse> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);

        EpisX26Type episX26Type = episMessageResponseStore.pop(message.getId(), EpisX26Type.class);
        EpisX26ResponseType response = episX26Type.getResponse();
        Integer resultCode = response.getResults().getResultCode();

        if (resultCode != null) {
            if (resultCode == 40544) {
                log.info("Person {} has not activated second pillar", personalCode);
            } else {
                log.error("Unknown result code {}", resultCode);
            }
            return emptyList();
        }

        if (response.getApplications() == null) {
            log.info("Person {} doesn't have any applications", personalCode);
            return emptyList();
        }

        return converter.convert(
            response.getApplications().getApplicationOrExchangeApplicationOrFundPensionOpen()
        );
    }

    private EpisMessage sendQuery(String personalCode) {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX26RequestType request = episMessageFactory.createEpisX26RequestType();
        request.setPersonalData(personalData);

        EpisX26Type episX26Type = episMessageFactory.createEpisX26Type();
        episX26Type.setRequest(request);

        JAXBElement<EpisX26Type> applicationListRequest =
            episMessageFactory.createAVALDUSTELOETELU(episX26Type);

        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, applicationListRequest);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());

        return episMessage;
    }

}
