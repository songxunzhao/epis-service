package ee.tuleva.epis

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

@SpringBootTest
class EpisServiceApplicationSpec extends Specification {

    @Autowired
    WebApplicationContext context

    static {
        System.setProperty("MHUB_KEYSTORE", "test_keys/truststore.jks")
        System.setProperty("MHUB_KEYSTORE_PASSWORD", "changeit")
        System.setProperty("MHUB_USERID", "foo")
        System.setProperty("MHUB_PASSWORD", "foo")
    }

    def "context loads"() {
        expect:
        context != null
    }
}
