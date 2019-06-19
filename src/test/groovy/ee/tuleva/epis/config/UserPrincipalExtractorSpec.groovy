package ee.tuleva.epis.config

import spock.lang.Specification

class UserPrincipalExtractorSpec extends Specification {

    def "ExtractPrincipal"() {
        given:
        def principalExtractor = new UserPrincipalExtractor()
        def input = [
            "firstName"   : "Erko",
            "lastName"    : "Risthein",
            "personalCode": "38585858585"
        ]

        when:
        UserPrincipal principal = principalExtractor.extractPrincipal(input)

        then:
        principal.firstName == input.firstName
        principal.lastName == input.lastName
        principal.personalCode == input.personalCode
    }
}
