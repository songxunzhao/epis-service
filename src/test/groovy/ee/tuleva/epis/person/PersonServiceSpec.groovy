package ee.tuleva.epis.person

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification
//@ContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class PersonServiceSpec extends Specification {

    @Autowired
    PersonService service;

    def "gets a person"() {
        given:
        String idCode = "38080808080"
        when:
        Person person = service.getPerson(idCode)
        then:
        person.firstName == "first name";

    }

}
