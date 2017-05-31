package ee.tuleva.epis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

@SpringBootTest
class EpisServiceApplicationSpec extends Specification {

    @Autowired
    WebApplicationContext context

    def "context loads"() {
        expect:
        context != null
    }
}