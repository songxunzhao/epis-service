package ee.tuleva.epis.epis

import org.springframework.jms.core.JmsTemplate
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import spock.lang.Specification

class EpisServiceSpec extends Specification {

    JmsTemplate jmsTemplate = Mock(JmsTemplate)
    Jaxb2Marshaller marshaller = Mock(Jaxb2Marshaller)
    EpisService service = new EpisService(jmsTemplate, marshaller)

    def "send: sends messages"() {
        given:
        String sampleMessage = "this is a message"

        when:
        service.send(sampleMessage)

        then:
        1 * jmsTemplate.send("BMMH.TULEVAP.IN", _ as EpisService.MandateProcessorMessageCreator)
    }

}
