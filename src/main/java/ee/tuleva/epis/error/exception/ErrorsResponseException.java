package ee.tuleva.epis.error.exception;

import ee.tuleva.epis.error.response.ErrorsResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Exception to propagate ErrorsResponse to ControllerAdvice
 */
@Getter
@AllArgsConstructor
public class ErrorsResponseException extends RuntimeException {

    ErrorsResponse errorsResponse;

    public ErrorsResponseException(ErrorsResponse errorsResponse, String message) {
        super(message);
        this.errorsResponse = errorsResponse;
    }

    public ErrorsResponseException(ErrorsResponse errorsResponse, String message, Throwable cause) {
        super(message, cause);
        this.errorsResponse = errorsResponse;
    }
}
