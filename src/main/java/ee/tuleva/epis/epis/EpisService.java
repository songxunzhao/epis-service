package ee.tuleva.epis.epis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Session;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisService {

    private final JmsTemplate jmsTemplate;

    public void send(String message) {
        log.info("Sending message with hashCode {}", message.hashCode());
        jmsTemplate.send("MHUB.PRIVATE.IN", new EpisService.MandateProcessorMessageCreator(message));
    }

    class MandateProcessorMessageCreator implements MessageCreator {

        private String message;

        MandateProcessorMessageCreator(String message) {
            this.message = message;
        }

        @Override
        public javax.jms.Message createMessage(Session session) throws JMSException {
            // TODO: .createObjectMessage(
            return session.createTextMessage(message);
        }
    }

}
