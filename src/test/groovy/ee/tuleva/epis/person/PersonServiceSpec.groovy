package ee.tuleva.epis.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.jms.Message
import javax.jms.MessageListener

@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class PersonServiceSpec extends Specification {

    @Autowired
    PersonService service;

    @Bean
    public MessageListener processorListener() {
        return new MessageListener() {
            @Override
            public void onMessage(Message message) {
                println(message);
            }
        };
    }

    def "gets a person"() {
        given:
        String idCode = "38080808080"
        when:
        Person person = service.getPerson(idCode)
        then:
        person.firstName == "first name";

    }

}
