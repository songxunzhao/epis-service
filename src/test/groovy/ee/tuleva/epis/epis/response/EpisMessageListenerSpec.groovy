package ee.tuleva.epis.epis.response

import com.fasterxml.jackson.databind.ObjectMapper
import ee.tuleva.epis.epis.EpisMessageType
import ee.tuleva.epis.mandate.processor.MandateProcess
import ee.tuleva.epis.mandate.processor.MandateProcessRepository
import ee.tuleva.epis.mandate.processor.MandateProcessResult
import iso.std.iso._20022.tech.xsd.head_001_001.BusinessApplicationHeaderV01
import mhub.xsd.envelope._01.Ex
import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageConverter
import spock.lang.Specification

import javax.jms.Message
import javax.xml.bind.JAXBElement

class EpisMessageListenerSpec extends Specification {

    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    MessageConverter messageConverter = Mock(MarshallingMessageConverter)
    ObjectMapper objectMapper = new ObjectMapper()
    MandateProcessRepository mandateProcessRepository = Mock(MandateProcessRepository)
    EpisMessageResponseHandler mandateMessageResponseHandler = Mock(EpisMessageResponseHandler)

    EpisMessageListener service = new EpisMessageListener(
            episMessageResponseStore,
            messageConverter,
            objectMapper,
            mandateMessageResponseHandler,
            mandateProcessRepository
    )

    def "On message, store it"() {
        given:

        String sampleId = 'sampleId';

        1 * mandateMessageResponseHandler.getMessageType(sampleMessage) >> Optional.of(EpisMessageType.LIST_APPLICATIONS)

        messageConverter.fromMessage(sampleMessage) >> Mock(Ex, {
            getBizMsg() >> Mock(Ex.BizMsg, {
                getAppHdr() >> Mock(BusinessApplicationHeaderV01, {
                    getBizMsgIdr() >> sampleId
                })
                getAny() >> Mock(JAXBElement,{
                    getValue() >> [:]
                })
            })
        })

        when:
        service.processorListener().onMessage(sampleMessage)
        then:
        1 * episMessageResponseStore.storeOne(
                sampleId, "{}"
        );
    }

    //FIXME: this functionality needs to be removed
    //and generalized to using response store
    def "On application process message, persist it"() {
        given:
        String sampleProcessId = "123"

        1 * mandateMessageResponseHandler.getMessageType(sampleMessage) >> Optional.of(EpisMessageType.APPLICATION_PROCESS)

        1 * mandateMessageResponseHandler.getMandateProcessResponse(sampleMessage) >>
                MandateProcessResult.builder()
                        .processId(sampleProcessId)
                        .successful(true)
                        .build()

        1 * mandateProcessRepository.findOneByProcessId(sampleProcessId) >>
                MandateProcess.builder()
                        .processId(sampleProcessId)
                        .build()

        when:
        service.processorListener().onMessage(sampleMessage)
        then:
        1 * mandateProcessRepository.save({ MandateProcess process ->
            process.processId == sampleProcessId && process.isSuccessful().get() == true
        })

    }

    Message sampleMessage = Mock(Message)
}
