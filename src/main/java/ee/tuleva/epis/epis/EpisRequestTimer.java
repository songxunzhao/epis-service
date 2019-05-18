package ee.tuleva.epis.epis;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class EpisRequestTimer {

    private final ConcurrentMap<String, StopWatch> stopWatches = new ConcurrentHashMap<>();

    public void start(String id) {
        StopWatch stopWatch = new StopWatch(id);
        stopWatch.start();
        stopWatches.put(id, stopWatch);
    }

    public void stop(String id) {
        val stopWatch = stopWatches.get(id);
        if (stopWatch != null) {
            stopWatch.stop();
            log.info("Epis request {} took {} ms", id, stopWatch.getTotalTimeMillis());
        } else {
            log.warn("No stopwatch for id {}", id);
        }
    }
}
