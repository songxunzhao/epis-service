package ee.tuleva.onboarding.mandate.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateApplicationListService {

    private final JmsTemplate jmsTemplate;

    public List<MandateApplicationResponse> get(String personalCode) {

        return null;
    }

}
