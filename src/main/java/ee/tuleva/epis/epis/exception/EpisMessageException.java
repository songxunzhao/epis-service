package ee.tuleva.epis.epis.exception;

public class EpisMessageException extends RuntimeException {

    public EpisMessageException(String message) {
        super(message);
    }

    public EpisMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}