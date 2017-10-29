package ee.tuleva.epis.epis.response;

import com.ibm.jms.JMSBytesMessage;
import ee.tuleva.epis.epis.EpisMessageType;
import ee.tuleva.epis.gen.*;
import ee.tuleva.epis.mandate.application.list.EpisApplicationListResponse;
import ee.tuleva.epis.mandate.processor.MandateProcessResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

// FIXME: remove this class,
@Service
@Slf4j
@RequiredArgsConstructor
public class EpisMessageResponseHandler {

    private final ee.tuleva.epis.epis.response.EpisMessageResponseReader episMessageResponseReader;

    public Optional<EpisMessageType> getMessageType(Message message) {
        log.info("Identifying message with hash {}", message.hashCode());

        MHubEnvelope mHubEnvelope = messageToMHubEnvelope(message);
        JAXBElement jaxbElement = mHubEnvelopeToJAXBElement(mHubEnvelope);
        Optional<EpisMessageType> episMessageType = jaxbElementToEpisMessageType(jaxbElement);
        String id = getMHubEnvelopeId(mHubEnvelope);

        log.info("Message with id {} and hash {} is of type {}", id, message.hashCode(), episMessageType);

        resetMessage(message);

        return episMessageType;
    }

    private void resetMessage(Message message) {
        try {
            if(message instanceof JMSBytesMessage) {
                ((JMSBytesMessage) message).reset();
            }
        } catch (JMSException e) {
            log.error("Couldn't reset message after determining type", e.getMessage());
        }
    }

    public MandateProcessResult getMandateProcessResponse(Message message) {
        log.info("Message received");

        MHubEnvelope mHubEnvelope = messageToMHubEnvelope(message);

        String id = getMHubEnvelopeId(mHubEnvelope);
        JAXBElement jaxbElement = mHubEnvelopeToJAXBElement(mHubEnvelope);

        log.info("Getting response for message id {}", id);
        MHubResponse response = getResponse(jaxbElement, id);

        if(response.isSuccess()) {
            log.info("Message id {} returned successful response", id);
        } else {
            log.info("Message id {} returned error response with code {}", response.getErrorCode().toString());
        }

        return MandateProcessResult.builder()
                .processId(id)
                .successful(response.isSuccess())
                .errorCode(response.getErrorCode())
                .build();
    }

    public EpisApplicationListResponse getApplicationListResponse(Message message) {
        MHubEnvelope mHubEnvelope = messageToMHubEnvelope(message);

        String id = getMHubEnvelopeId(mHubEnvelope);
        JAXBElement jaxbElement = mHubEnvelopeToJAXBElement(mHubEnvelope);
        Object jaxbObject = jaxbElement.getValue();

        log.info("Getting applications list from message with id {}", id);

        if(jaxbObject instanceof EpisX26Type) { // applications list response
            List<ApplicationType> applications = ((EpisX26Type) jaxbObject).getResponse().getApplications()
                    .getApplicationOrExchangeApplicationOrFundPensionOpen();

            return EpisApplicationListResponse.builder()
                    .applications(applications)
                    .id(id)
                    .build();
        } else {
            throw new RuntimeException("Trying to extract applications list from unknown JAXB response type.");
        }
    }

    public Object getPersonalDataResponse(Message message) {
        //

        return new Object();
    }

    private MHubResponse getResponse(JAXBElement jaxbElement, String id) {
        MHubResponse response = new MHubResponse();
        Object jaxbObject = jaxbElement.getValue();

        if (jaxbObject instanceof EpisX5Type) { // application processing response
            response.setSuccess(((EpisX5Type) jaxbObject).getResponse().getResults().getResult().equals(AnswerType.OK));
            response.setErrorCode(((EpisX5Type) jaxbObject).getResponse().getResults().getResultCode());
            return response;
        } else if (jaxbObject instanceof EpisX6Type) {  // application processing response
            response.setSuccess((((EpisX6Type) jaxbObject).getResponse().getResults().getResult().equals(AnswerType.OK)));
            response.setErrorCode(((EpisX6Type) jaxbObject).getResponse().getResults().getResultCode());
            return response;
        }

        log.error("Couldn't find message instance type");
        response.setSuccess(false);
        return response;
    }

    private MHubEnvelope messageToMHubEnvelope(Message message) {
        String messageText = episMessageResponseReader.getText(message);
        MHubEnvelope envelope = unmarshallMessage(messageText, MHubEnvelope.class);

        if(envelope == null) {
            throw new RuntimeException("Can't parse response");
            // todo: try error types or handle error
        }
        return envelope;
    }

    private String getMHubEnvelopeId(MHubEnvelope mHubEnvelope) {
        return mHubEnvelope.getBizMsg().getAppHdr().getBizMsgIdr();
    }

    private JAXBElement mHubEnvelopeToJAXBElement(MHubEnvelope mHubEnvelope) {
        Element element = mHubEnvelope.getBizMsg().getAny();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("ee.tuleva.epis.gen");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement jaxbElement = (JAXBElement) jaxbUnmarshaller.unmarshal(element);
            return jaxbElement;
        } catch (JAXBException e) {
            log.error("Exception on return message parsing", e);
            throw new RuntimeException(e);
        }
    }

    private Optional<EpisMessageType> jaxbElementToEpisMessageType(JAXBElement jaxbElement) {
        Object jaxbObject = jaxbElement.getValue();

        EpisMessageType episMessageType = null;

        if (jaxbObject instanceof EpisX5Type) { // application processing response
            episMessageType = EpisMessageType.APPLICATION_PROCESS;
        } else if (jaxbObject instanceof EpisX6Type) {  // application processing response
            episMessageType = EpisMessageType.APPLICATION_PROCESS;
        } else if (jaxbObject instanceof EpisX26Type) { // applications list response
            episMessageType = EpisMessageType.LIST_APPLICATIONS;
        } else if (jaxbObject instanceof EpisX12Type) {
            episMessageType = EpisMessageType.PERSONAL_DATA;
        } else {
            log.info("Didn't recognize epis message {}", jaxbElement);
        }

        return Optional.ofNullable(episMessageType);
    }

    private static <T> T unmarshallMessage(String msg, Class<T> expectedType) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("ee.tuleva.epis.gen");
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Object ret = jaxbUnmarshaller.unmarshal(new StringReader(msg));

            if (ret instanceof JAXBElement) {
                JAXBElement jaxbElement = (JAXBElement) ret;
                Object valueObj = jaxbElement.getValue();

                if (expectedType.isInstance(valueObj)) {
                    return (T) valueObj;
                }
            }

            log.warn("Unable to parse Mhub message, unexpected return type!");
        } catch (JAXBException e) {
            log.warn("Unable to parse MHub message!" , e);
        }

        return null;
    }

    @Getter
    @Setter
    private class MHubResponse {
        private boolean success;
        private Integer errorCode;
    }
}
