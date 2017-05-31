package ee.tuleva.epis.epis.response;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpisMessageResponseStore {

    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    private static final String CHANNEL_PREFIX = "EPIS_MESSAGE_";

    public void storeOne(String id, Object content) {
        String queName = getQueName(id);

        if(!doesQueExist(queName)) {
            createQue(queName);
        }

        log.info("Sending to AMQP que {}", queName);
        amqpTemplate.convertAndSend(queName, content.toString());
    }

    public Object pop(String id) {
        String queName = getQueName(id);

        if(!doesQueExist(queName)) {
            createQue(queName);
        }

        log.info("Waiting for mandate applications response at que {}", queName);
        // This is a blocking call
//        Message response = amqpTemplate.receive(queName, 10000);
        Object o = amqpTemplate.receiveAndConvert(queName, 10000);
        log.info("Got response");

        amqpAdmin.deleteQueue(queName);

        return o;
//        return response.getBody();
    }

    private String getQueName(String id) {
        return CHANNEL_PREFIX.concat(id);
    }

    private boolean doesQueExist(String queName) {
        return amqpAdmin.getQueueProperties(queName) != null;
    }

    private void createQue(String queName) {
        log.info("Creating AMQP que {}", queName);
        Queue queue = new Queue(queName, true, false, true);
        amqpAdmin.declareQueue(queue);
    }

}
