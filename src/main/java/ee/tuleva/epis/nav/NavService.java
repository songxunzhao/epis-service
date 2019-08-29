package ee.tuleva.epis.nav;

import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.x_road.epis.producer.EpisX17ResponseType;
import ee.x_road.epis.producer.EpisX17Type;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NavService {

    private final EpisService episService;
    private final EpisMessageWrapper episMessageWrapper;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisX17RequestFactory requestFactory;
    private final EpisX17ResponseConverter responseConverter;

    Optional<NavData> getNavData(String isin, LocalDate date) {
        JAXBElement<EpisX17Type> request = requestFactory.create(isin, date);
        EpisMessage message = episMessageWrapper.createWrappedMessage(request);
        episService.send(message);
        EpisX17ResponseType response = episMessageResponseStore.pop(message.getId(), EpisX17Type.class).getResponse();
        return responseConverter.convert(response);
    }
}
