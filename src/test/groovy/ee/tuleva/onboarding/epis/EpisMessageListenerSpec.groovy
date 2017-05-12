package ee.tuleva.onboarding.epis

import ee.tuleva.onboarding.mandate.processor.MandateProcess
import ee.tuleva.onboarding.mandate.processor.MandateProcessRepository
import ee.tuleva.onboarding.mandate.processor.MandateProcessResult
import spock.lang.Specification

import javax.jms.Message

class EpisMessageListenerSpec extends Specification {

    MandateProcessRepository mandateProcessRepository = Mock(MandateProcessRepository)
    EpisMessageResponseHandler mandateMessageResponseHandler = Mock(EpisMessageResponseHandler)

    EpisMessageListener service = new EpisMessageListener(mandateProcessRepository, mandateMessageResponseHandler)

    def "ProcessorListener: On message, persist it"() {
        given:
        String sampleProcessId = "123"

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
