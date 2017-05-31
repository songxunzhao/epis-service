package ee.tuleva.epis.error.response

import spock.lang.Specification

class ErrorsResponseSpec extends Specification {

    def "HasErrors: return false if no errors"() {
        when:
        ErrorsResponse errors = new ErrorsResponse([])
        then:
        errors.hasErrors() == false
    }

    def "HasErrors: return true if errors"() {
        when:
        ErrorsResponse errors = new ErrorsResponse([[:]])
        then:
        errors.hasErrors() == true
    }

}
