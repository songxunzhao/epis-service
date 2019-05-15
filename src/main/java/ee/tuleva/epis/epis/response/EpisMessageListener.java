package ee.tuleva.epis.epis.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.tuleva.epis.epis.exception.EpisMessageException;
import ee.tuleva.epis.mandate.processor.MandateProcess;
import ee.tuleva.epis.mandate.processor.MandateProcessRepository;
import ee.tuleva.epis.mandate.processor.MandateProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

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
    private final EpisMessageResponseHandler episMessageResponseHandler;
    private final MandateProcessRepository mandateProcessRepository;

    @Bean
    public MessageListener processorListener() {
        return message -> {
            log.info("Got message from MHub: {}", message);

//            Optional<EpisMessageType> episMessageType = episMessageResponseHandler.getMessageType(message);
//
//            //FIXME: get rid of custom handling for application process,
//            //everything should go through response store
//            boolean isApplicationProcessResponse = episMessageType
//                    .map(type -> type == EpisMessageType.APPLICATION_PROCESS)
//                    .orElse(false);
//
//            if (isApplicationProcessResponse) {
//                log.info("Application process response");
//                handleApplicationProcessResponse(message);
//            } else {
//                log.info("Storing in response store");
//                store(message);
//            }

            log.info("Storing in response store");
            store(message);
        };
    }

    private void store(Message message) {
        Ex envelope = getEnvelope(message);
        episMessageResponseStore.storeOne(
            getMessageId(envelope),
            getResponse(envelope)
        );
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

    /* TODO: move this logic to onboarding-service */
    private void handleApplicationProcessResponse(Message message) {
        log.info("Process result received");
        MandateProcessResult mandateProcessResult =
            episMessageResponseHandler.getMandateProcessResponse(message);

        log.info("Process result with id {} received", mandateProcessResult.getProcessId());
        MandateProcess process = mandateProcessRepository.findOneByProcessId(mandateProcessResult.getProcessId());
        process.setSuccessful(mandateProcessResult.isSuccessful());
        process.setErrorCode(mandateProcessResult.getErrorCode().orElse(null));

        if (process.getErrorCode().isPresent()) {
            log.info("Process with id {} is {} with error code {}",
                process.getId(),
                process.isSuccessful().toString(),
                process.getErrorCode().toString()
            );

        } else {
            log.info("Process with id {} is {}",
                process.getId(),
                process.isSuccessful().toString());
        }

        mandateProcessRepository.save(process);
    }
}
