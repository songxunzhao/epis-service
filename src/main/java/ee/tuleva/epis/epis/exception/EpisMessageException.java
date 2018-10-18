package ee.tuleva.epis.epis.exception;

import ee.tuleva.epis.error.exception.ErrorsResponseException;
import ee.tuleva.epis.error.response.ErrorsResponse;

public class EpisMessageException extends ErrorsResponseException {

    public EpisMessageException(String message) {
        super(ErrorsResponse.ofSingleError(
            "epis.message.exception",
            message
        ), message);
    }

    public EpisMessageException(String message, Throwable cause) {
        super(ErrorsResponse.ofSingleError(
            "epis.message.exception",
            message
        ), message, cause);
    }
}