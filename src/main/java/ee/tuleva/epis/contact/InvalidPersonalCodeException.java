package ee.tuleva.epis.contact;

class InvalidPersonalCodeException extends RuntimeException {
    InvalidPersonalCodeException(String message) {
        super(message);
    }
}
