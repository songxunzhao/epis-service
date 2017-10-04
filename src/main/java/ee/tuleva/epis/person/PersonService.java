package ee.tuleva.epis.person;

import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageService;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.person.request.MessageCreator;
import ee.x_road.epis.producer.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3._2003._05.soap_envelope.Body;
import org.w3._2003._05.soap_envelope.Envelope;
import org.w3._2003._05.soap_envelope.Header;
import org.w3._2003._05.soap_envelope.ObjectFactory;

import javax.xml.bind.JAXBElement;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonService {

    private final EpisService episService;
    private final EpisMessageService episMessageService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final MessageCreator messageCreator;

    Person getPerson(String personalCode) {

        EpisMessage message = sendQuery(personalCode);

        Person person = episMessageResponseStore.pop(message.getId(), Person.class);
//        Person person = new Person(personalCode, "first name", "last name");
        // get personalSelection

        return person;
    }

    private EpisMessage sendQuery(String personalCode) {
        String id = UUID.randomUUID().toString().replace("-", "");

        ObjectFactory envelopeFactory = new ObjectFactory();
        ee.x_road.xsd.x_road.ObjectFactory xRoadFactory = new ee.x_road.xsd.x_road.ObjectFactory();
        ee.x_road.epis.producer.ObjectFactory episFactory = new ee.x_road.epis.producer.ObjectFactory();

        PersonDataRequestType personalData = episFactory.createPersonDataRequestType();
        personalData.setPersonId("44806234555");

        EpisX12RequestType request = episFactory.createEpisX12RequestType();
        request.setPersonalData(personalData);

        EpisX12Type episX12Type = episFactory.createEpisX12Type();
        episX12Type.setRequest(request);

        JAXBElement<EpisX12Type> isikuAndmed = episFactory.createISIKUANDMED(episX12Type);
        Body body = envelopeFactory.createBody();
        body.getAny().add(isikuAndmed);

        Header header = envelopeFactory.createHeader();
        JAXBElement<String> consumer = xRoadFactory.createConsumer("XMLTULEVA");
        header.getAny().add(consumer);

        Envelope envelope = envelopeFactory.createEnvelope();
        envelope.setHeader(header);
        envelope.setBody(body);

        JAXBElement<Envelope> wrappedEnvelope = envelopeFactory.createEnvelope(envelope);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(wrappedEnvelope)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());

        return episMessage;
    }


}
