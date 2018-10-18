package ee.tuleva.epis.error;

import ee.tuleva.epis.error.exception.ErrorsResponseException;
import ee.tuleva.epis.error.response.ErrorResponseEntityFactory;
import ee.tuleva.epis.error.response.ErrorsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ErrorHandlingControllerAdvice {

    private final ErrorResponseEntityFactory errorResponseEntityFactory;

    @ExceptionHandler(ValidationErrorsException.class)
    public ResponseEntity<ErrorsResponse> handleErrors(ValidationErrorsException exception) {
        return errorResponseEntityFactory.fromErrors(exception.getErrors());
    }

    @ExceptionHandler(ErrorsResponseException.class)
    public ResponseEntity<ErrorsResponse> handleErrors(ErrorsResponseException exception) {
        log.error(exception.getMessage(), exception);
        return new ResponseEntity<>(exception.getErrorsResponse(), BAD_REQUEST);
    }

}
