package ee.tuleva.epis.mandate.processor.implementation

import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.mandate.application.MandateApplicationType
import ee.tuleva.epis.mandate.processor.MandateProcess
import ee.tuleva.epis.mandate.processor.MandateProcessRepository
import spock.lang.Specification

import java.time.Clock
import java.time.Instant

class MhubProcessRunnerSpec extends Specification {

    MandateProcessRepository mandateProcessRepository = Mock(MandateProcessRepository)
    EpisService episService = Mock(EpisService)
    Clock clock = Mock(Clock)

    MhubProcessRunner service = new MhubProcessRunner(mandateProcessRepository, episService, clock)

    String sampleProcessId1 = "123"
    String sampleProcessId2 = "124"

    def "process: process messages synchronously: finish one message and then start another"() {
        given:
        clock.instant() >> Instant.ofEpochSecond(0)
        1 * episService.send(sampleMessages.get(0).message)
        1 * episService.send(sampleMessages.get(1).message)
        2 * mandateProcessRepository.findOneByProcessId(sampleProcessId1) >>
                MandateProcess.builder()
                        .processId(sampleProcessId1)
                        .successful(true)
                        .build()
        when:
        service.process(sampleMessages)
        then:
        2 * mandateProcessRepository.findOneByProcessId(sampleProcessId2) >>
                MandateProcess.builder()
                        .processId(sampleProcessId2)
                        .successful(true)
                        .build()
    }

    def "process: process messages times out after the timeout period and doesn't infinite loop"() {
        given:
        clock.instant() >> Instant.ofEpochSecond(0)
        mandateProcessRepository.findOneByProcessId(sampleProcessId1) >>
                MandateProcess.builder()
                        .processId(sampleProcessId1)
                        .build()

        when:
        service.process([sampleMessages.first()])
        clock.instant() >> Instant.ofEpochSecond(MhubProcessRunner.TIMEOUT_SECONDS)

        then:
        true
    }


    List<String> sampleMessages = [
            new MandateXmlMessage(sampleProcessId1, "message", MandateApplicationType.TRANSFER),
            new MandateXmlMessage(sampleProcessId2, "message", MandateApplicationType.TRANSFER)
    ]

}
