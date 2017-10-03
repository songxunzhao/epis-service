package ee.tuleva.epis.person;

import ee.tuleva.epis.epis.EpisMessageType;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageService;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.person.request.MessageCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        EpisMessage episMessage = episMessageService.get(
                EpisMessageType.PERSONAL_DATA,
                messageCreator.getMessage(personalCode)
        );

        episService.send(episMessage.getContent());

        return episMessage;
    }


}
