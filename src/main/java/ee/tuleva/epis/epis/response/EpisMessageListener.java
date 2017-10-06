package ee.tuleva.epis.epis.response;

import ee.tuleva.epis.epis.response.application.list.EpisApplicationListResponse;
import ee.tuleva.epis.epis.response.application.list.EpisApplicationListToMandateApplicationResponseListConverter;
import ee.tuleva.epis.mandate.processor.MandateProcess;
import ee.tuleva.epis.mandate.processor.MandateProcessRepository;
import ee.tuleva.epis.mandate.processor.MandateProcessResult;
import ee.tuleva.epis.person.Person;
import ee.x_road.epis.producer.EpisX12ResponseType;
import ee.x_road.epis.producer.EpisX12Type;
import ee.x_road.epis.producer.PersonType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.codehaus.jackson.map.ObjectMapper;
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

    private final MandateProcessRepository mandateProcessRepository;
    private final ee.tuleva.epis.epis.response.EpisMessageResponseHandler episMessageResponseHandler;
    private final ee.tuleva.epis.epis.response.EpisMessageResponseStore episMessageResponseStore;
    private final EpisApplicationListToMandateApplicationResponseListConverter applicationListConverter;
    private final MessageConverter messageConverter;

    @Bean
    public MessageListener processorListener() {
        return new MessageListener() {
            @Override
            public void onMessage(Message message) {

                EpisX12ResponseType response = getResponse(message);

                PersonType personResponse = response.getPersonalData();
                Person person = new Person(
                    personResponse.getPersonId(),
                    personResponse.getFirstName(),
                    personResponse.getName());

                log.debug(person.toString());

//                EpisMessageType episMessageType = episMessageResponseHandler.getMessageType(message);

//                if (episMessageType == EpisMessageType.LIST_APPLICATIONS) {
//                    handleListApplicationsResponse(message);
//                } else if (episMessageType == EpisMessageType.APPLICATION_PROCESS) {
//                    handleApplicationProcessResponse(message);
//                } else if (episMessageType == EpisMessageType.PERSONAL_DATA) {
//                    handlePersonalDataResponse(message);
//                }
            }
        };
    }

    private EpisX12ResponseType getResponse(Message message) {
        try {
            Ex ex = (Ex) messageConverter.fromMessage(message);
            return ((EpisX12Type) ((JAXBElement) ex.getBizMsg().getAny()).getValue()).getResponse();
        } catch (JMSException e) {
            log.error(e.getMessage(), e);
            return null;
        }
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
