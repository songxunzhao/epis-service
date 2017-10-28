package ee.tuleva.epis.mandate.application;

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
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final EpisApplicationListToMandateApplicationResponseListConverter converter;

    public List<MandateExchangeApplicationResponse> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);

        EpisX26ResponseType response = episMessageResponseStore.pop(message.getId(), EpisX26ResponseType.class);

        return converter.convert(
                response.getApplications().getApplicationOrExchangeApplicationOrFundPensionOpen()
        );
    }

    private EpisMessage sendQuery(String personalCode) {
        ee.x_road.epis.producer.ObjectFactory episFactory = new ee.x_road.epis.producer.ObjectFactory();

        PersonDataRequestType personalData = episFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX26RequestType request = episFactory.createEpisX26RequestType();
        request.setPersonalData(personalData);

        EpisX26Type episX26Type = episFactory.createEpisX26Type();
        episX26Type.setRequest(request);

        JAXBElement<EpisX26Type> applicationListRequest =
                episFactory.createAVALDUSTELOETELU(episX26Type);

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
