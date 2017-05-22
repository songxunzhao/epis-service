package ee.tuleva.onboarding.mandate.processor.implementation;

import ee.tuleva.onboarding.epis.EpisService;
import ee.tuleva.onboarding.mandate.processor.MandateProcess;
import ee.tuleva.onboarding.mandate.processor.MandateProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MhubProcessRunner {
    private final MandateProcessRepository mandateProcessRepository;
    private final EpisService episService;

    @Async
    public void process(List<MandateXmlMessage> messages) {
        messages.forEach( message -> {
            MandateProcess process = mandateProcessRepository.findOneByProcessId(message.getProcessId());

            log.info("Starting process with id {} and type {}", message.getProcessId(), message.getType().toString());
            episService.send(message.getMessage());
            log.info("Sent message for process {}", message.getProcessId());
            waitForProcessToFinish(process);
        });
    }

    // EPIS message que NEEDS SYNCHRONIZATION,
    // before you send a new message, older one needs a response
    // otherwise it responds with a technical error
    private void waitForProcessToFinish(MandateProcess process) {
        while(isProcessFinished(process) != true) {
            log.info("Waiting for process id {} to finish", process.getId());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isProcessFinished(MandateProcess inputProcess) {
        MandateProcess process = mandateProcessRepository.findOneByProcessId(inputProcess.getProcessId());
        return process.isSuccessful().isPresent();
    }

}