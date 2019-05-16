package ee.tuleva.epis.epis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisService {

    private final JmsTemplate jmsTemplate;
    private final Jaxb2Marshaller marshaller;

    public void send(String message) {
        log.info("Sending message with hashCode {}", message.hashCode());
        jmsTemplate.send("BMMH.TULEVAP.IN", new EpisService.MandateProcessorMessageCreator(message));
    }

    public void send(Object message) {
        log.info("Sending message with hashCode {}", message.hashCode());
        log(message);
        jmsTemplate.convertAndSend("BMMH.TULEVAP.IN", message);
    }

    private void log(Object message) {
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(message, new StreamResult(stringWriter));
        log.debug(stringWriter.toString());
    }

    class MandateProcessorMessageCreator implements MessageCreator {
        private String message;

        MandateProcessorMessageCreator(String message) {
            this.message = message;
        }

        @Override
        public javax.jms.Message createMessage(Session session) throws JMSException {
            return session.createTextMessage(message);
        }
    }

}
