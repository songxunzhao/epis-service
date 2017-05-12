package ee.tuleva.onboarding.mandate.application;

import ee.tuleva.onboarding.epis.EpisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListService {

    private final EpisService episService;

    public List<MandateApplicationResponse> get(String personalCode) {

        return null;
    }

}
