package ee.tuleva.epis.mandate.processor.implementation

import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.mandate.application.MandateApplicationType
import ee.tuleva.epis.mandate.processor.MandateProcess
import ee.tuleva.epis.mandate.processor.MandateProcessRepository
import spock.lang.Specification

class MhubProcessRunnerSpec extends Specification {

    MandateProcessRepository mandateProcessRepository = Mock(MandateProcessRepository)
    EpisService episService = Mock(EpisService)

    MhubProcessRunner service = new MhubProcessRunner(mandateProcessRepository, episService)

    String sampleProcessId1 = "123"
    String sampleProcessId2 = "124"

    def "process: process messages synchronously: finish one message and then start another"() {
        given:
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


    List<String> sampleMessages = [
            new MandateXmlMessage(sampleProcessId1, "message", MandateApplicationType.TRANSFER),
            new MandateXmlMessage(sampleProcessId2, "message", MandateApplicationType.TRANSFER)
    ]

}
