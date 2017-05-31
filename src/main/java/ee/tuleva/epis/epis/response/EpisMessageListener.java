package ee.tuleva.epis.epis.response;

import ee.tuleva.epis.epis.EpisMessageType;
import ee.tuleva.epis.epis.response.application.list.EpisApplicationListResponse;
import ee.tuleva.epis.epis.response.application.list.EpisApplicationListToMandateApplicationResponseListConverter;
import ee.tuleva.epis.mandate.processor.MandateProcess;
import ee.tuleva.epis.mandate.processor.MandateProcessRepository;
import ee.tuleva.epis.mandate.processor.MandateProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EpisMessageListener {

    private final MandateProcessRepository mandateProcessRepository;
    private final ee.tuleva.epis.epis.response.EpisMessageResponseHandler episMessageResponseHandler;
    private final ee.tuleva.epis.epis.response.EpisMessageResponseStore episMessageResponseStore;
    private final EpisApplicationListToMandateApplicationResponseListConverter applicationListConverter;

    @Bean
    public MessageListener processorListener() {
        return new MessageListener() {
            @Override
            public void onMessage(Message message) {

                EpisMessageType episMessageType = episMessageResponseHandler.getMessageType(message);

                if(episMessageType == EpisMessageType.LIST_APPLICATIONS) {
                    handleListApplicationsResponse(message);
                } else if (episMessageType == EpisMessageType.APPLICATION_PROCESS) {
                    handleApplicationProcessResponse(message);
                }
            }
        };
    }

    private void handleListApplicationsResponse(Message message) {
        EpisApplicationListResponse episApplicationListResponse =
                episMessageResponseHandler.getApplicationListResponse(message);

        String json = null;
        try {
            json = (new ObjectMapper())
                    .writeValueAsString(applicationListConverter
                            .convert(episApplicationListResponse.getApplications()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        episMessageResponseStore.storeOne(
                episApplicationListResponse.getId(),
                json
        );
    }

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
