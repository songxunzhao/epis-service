package ee.tuleva.epis.epis;

import ee.tuleva.epis.epis.request.EpisMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisService {

    private final JmsTemplate jmsTemplate;
    private final Jaxb2Marshaller marshaller;
    private final EpisRequestTimer episRequestTimer;

    public void send(EpisMessage message) {
        val payload = message.getPayload();
        log.info("Sending message:");
        log(payload);
        episRequestTimer.start(message.getId());
        jmsTemplate.convertAndSend("BMMH.TULEVAP.IN", payload);
    }

    private void log(Object message) {
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(message, new StreamResult(stringWriter));
        log.info(stringWriter.toString());
    }

}
