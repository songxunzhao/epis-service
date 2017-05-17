package ee.tuleva.onboarding.epis.response;

import ee.tuleva.onboarding.epis.EpisMessageType;
import ee.tuleva.onboarding.mandate.processor.MandateProcess;
import ee.tuleva.onboarding.mandate.processor.MandateProcessRepository;
import ee.tuleva.onboarding.mandate.processor.MandateProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import javax.jms.MessageListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class EpisMessageListener {

    private final MandateProcessRepository mandateProcessRepository;
    private final EpisMessageResponseHandler episMessageResponseHandler;
    private final EpisMessageResponseStore episMessageResponseStore;

    @Bean
    public MessageListener processorListener() {
        return new MessageListener() {

            @Override
            public void onMessage(Message message) {

                EpisMessageType episMessageType = episMessageResponseHandler.getMessageType(message);

                if(episMessageType == EpisMessageType.LIST_APPLICATIONS) {
                    EpisApplicationListResponse episApplicationListResponse =
                            episMessageResponseHandler.getApplicationListResponse(message);

                    episMessageResponseStore.storeOne(
                            episApplicationListResponse.getId(),
                            episApplicationListResponse.getApplications().toString()
                    );

                } else if (episMessageType == EpisMessageType.APPLICATION_PROCESS) {

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
        };
    }

}
