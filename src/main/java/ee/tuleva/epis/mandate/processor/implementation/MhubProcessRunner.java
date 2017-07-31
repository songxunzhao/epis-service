package ee.tuleva.epis.mandate.processor.implementation;

import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.mandate.processor.MandateProcess;
import ee.tuleva.epis.mandate.processor.MandateProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MhubProcessRunner {

    private static final long TIMEOUT_SECONDS = 120;

    private final MandateProcessRepository mandateProcessRepository;
    private final EpisService episService;
    private final Clock clock;

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
        Instant timeoutExpired = Instant.now(clock).plus(TIMEOUT_SECONDS, ChronoUnit.SECONDS);
        while (!isProcessFinished(process)) {
            log.info("Waiting for process id {} to finish", process.getProcessId());
            if (Instant.now().isAfter(timeoutExpired)) {
                log.error("Process id {} timed out", process.getProcessId());
                break;
            }
            sleep(100);
        }
    }

    private void sleep(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isProcessFinished(MandateProcess inputProcess) {
        MandateProcess process = mandateProcessRepository.findOneByProcessId(inputProcess.getProcessId());
        return process.isSuccessful().isPresent();
    }

}