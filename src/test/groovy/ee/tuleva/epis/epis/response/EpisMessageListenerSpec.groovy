package ee.tuleva.epis.epis.response

import ee.tuleva.epis.epis.EpisMessageType
import ee.tuleva.epis.epis.response.application.list.EpisApplicationListToMandateApplicationResponseListConverter
import ee.tuleva.epis.mandate.processor.MandateProcess
import ee.tuleva.epis.mandate.processor.MandateProcessRepository
import ee.tuleva.epis.mandate.processor.MandateProcessResult
import spock.lang.Specification

import javax.jms.Message

class EpisMessageListenerSpec extends Specification {

    MandateProcessRepository mandateProcessRepository = Mock(MandateProcessRepository)
    EpisMessageResponseHandler mandateMessageResponseHandler = Mock(EpisMessageResponseHandler)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisApplicationListToMandateApplicationResponseListConverter applicationListConverter =
            Mock(EpisApplicationListToMandateApplicationResponseListConverter)

    EpisMessageListener service = new EpisMessageListener(
            mandateProcessRepository,
            mandateMessageResponseHandler,
            episMessageResponseStore,
            applicationListConverter
    )

    def "ProcessorListener: On message, persist it"() {
        given:
        String sampleProcessId = "123"

        1 * mandateMessageResponseHandler.getMessageType(sampleMessage) >> EpisMessageType.APPLICATION_PROCESS

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
