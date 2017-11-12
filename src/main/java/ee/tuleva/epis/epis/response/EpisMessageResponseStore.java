package ee.tuleva.epis.epis.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpisMessageResponseStore {

    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;
    private final ObjectMapper objectMapper;

    private static final String CHANNEL_PREFIX = "EPIS_MESSAGE_";

    public void storeOne(String id, Object content) {
        String queueName = getQueueName(id);

        if(!doesQueueExist(queueName)) {
            createQueue(queueName);
        }

        log.info("Sending to AMQP queue {}", queueName);
        amqpTemplate.convertAndSend(queueName, content.toString());
    }

    public <T> T pop(String id, Class<T> valueType) {
        try {
            String json = pop(id);
            return (T) objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String pop(String id) {
        String queueName = getQueueName(id);

        if(!doesQueueExist(queueName)) {
            createQueue(queueName);
        }

        log.info("Waiting for mandate applications response at queue {}", queueName);
        // This is a blocking call
        String response = (String) amqpTemplate.receiveAndConvert(queueName, 10000);
        log.info("Got response {}", response);

        amqpAdmin.deleteQueue(queueName);

        return response;
    }

    private String getQueueName(String id) {
        return CHANNEL_PREFIX.concat(id);
    }

    private boolean doesQueueExist(String queueName) {
        return amqpAdmin.getQueueProperties(queueName) != null;
    }

    private void createQueue(String queueName) {
        log.info("Creating AMQP queue {}", queueName);
        Queue queue = new Queue(queueName, true, false, true);
        amqpAdmin.declareQueue(queue);
    }

}
