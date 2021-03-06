package ee.tuleva.epis.epis.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.tuleva.epis.epis.EpisRequestTimer;
import ee.tuleva.epis.epis.exception.EpisMessageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mhub.xsd.envelope._01.Ex;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.bind.JAXBElement;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EpisMessageListener {

    private final EpisMessageResponseStore episMessageResponseStore;
    private final MessageConverter messageConverter;
    private final ObjectMapper objectMapper;
    private final EpisRequestTimer episRequestTimer;

    @Bean
    public MessageListener processorListener() {
        return message -> {
            log.info("Got message from MHub: {}", message);
            store(message);
        };
    }

    private void store(Message message) {
        Ex envelope = getEnvelope(message);
        val id = getMessageId(envelope);
        episRequestTimer.stop(id);
        episMessageResponseStore.storeOne(id, getResponse(envelope));
    }

    private Ex getEnvelope(Message message) {
        try {
            return (Ex) messageConverter.fromMessage(message);
        } catch (JMSException e) {
            throw new EpisMessageException("Couldn't convert EPIS response message to POJO", e);
        }
    }

    private String getMessageId(Ex envelope) {
        return envelope.getBizMsg().getAppHdr().getBizMsgIdr();
    }

    private String getResponse(Ex envelope) {
        try {
            Object element = ((JAXBElement) envelope.getBizMsg().getAny()).getValue();
            return objectMapper.writeValueAsString(element);
        } catch (IOException e) {
            throw new EpisMessageException("Couldn't stringify EPIS response", e);
        }
    }

}
