package ee.tuleva.epis.epis.validator;

import ee.tuleva.epis.epis.exception.EpisMessageException;
import ee.x_road.epis.producer.AnswerType;
import ee.x_road.epis.producer.ResultType;
import org.springframework.stereotype.Service;

@Service
public class EpisResultValidator {

    public void validate(ResultType result) {
        if (result.getResult().equals(AnswerType.NOK)) {
            throw new EpisMessageException("Got error code " + result.getResultCode() + " from EPIS: "
                + result.getErrorTextEng());
        }
    }
}
