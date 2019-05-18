package ee.tuleva.epis.epis

import ee.tuleva.epis.epis.request.EpisMessage
import org.springframework.jms.core.JmsTemplate
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import spock.lang.Specification

class EpisServiceSpec extends Specification {

    JmsTemplate jmsTemplate = Mock(JmsTemplate)
    Jaxb2Marshaller marshaller = Mock(Jaxb2Marshaller)
    EpisRequestTimer episRequestTimer = Mock()
    EpisService service = new EpisService(jmsTemplate, marshaller, episRequestTimer)

    def "send: sends messages"() {
        given:
        String sampleMessage = "this is a message"
        def id = 'messageid'
        when:
        service.send(EpisMessage.builder().payload(sampleMessage).id(id).build())

        then:
        1 * jmsTemplate.convertAndSend("BMMH.TULEVAP.IN", sampleMessage)
        1 * episRequestTimer.start(id)
    }

}
