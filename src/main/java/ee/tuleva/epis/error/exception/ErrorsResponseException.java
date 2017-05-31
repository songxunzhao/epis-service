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

}
