package ee.tuleva.epis.epis

import org.springframework.jms.core.JmsTemplate
import spock.lang.Specification

class EpisServiceSpec extends Specification {

    JmsTemplate jmsTemplate = Mock(JmsTemplate)
    EpisService service = new EpisService(jmsTemplate)

    def "send: sends messages"() {
        given:
        String sampleMessage = "this is a message"

        when:
        service.send(sampleMessage)

        then:
        1 * jmsTemplate.send("BMMH.TULEVAP.IN", _ as EpisService.MandateProcessorMessageCreator)
    }

}
