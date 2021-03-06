package ee.tuleva.epis.mandate


import spock.lang.Specification

import static ee.tuleva.epis.config.UserPrincipalFixture.userPrincipalFixture
import static ee.tuleva.epis.mandate.MandateCommandFixture.mandateCommandFixture
import static ee.tuleva.epis.mandate.MandateResponseFixture.mandateResponseFixture

class CompositeMandateServiceSpec extends Specification {

    MandateService secondPillarService = Mock()
    MandateService thirdPillarService = Mock()
    MandateService compositeMandateService = new CompositeMandateService([secondPillarService, thirdPillarService])

    def setup() {
        secondPillarService.supports(2) >> true
        thirdPillarService.supports(3) >> true
    }

    def "Delegates sendMandate() call to children"() {
        given:
        def principal = userPrincipalFixture()
        def mandateCommand =  mandateCommandFixture().build()
        def mandateResponse = mandateResponseFixture().build()
        secondPillarService.sendMandate(principal, mandateCommand) >> [mandateResponse]

        when:
        def mandateResponses = compositeMandateService.sendMandate(principal, mandateCommand)

        then:
        mandateResponses == [mandateResponse]
    }

    def "Delegates supports() call to children"() {
        given:
        def pillar2 = 2
        def pillar3 = 3

        expect:
        compositeMandateService.supports(pillar2)
        compositeMandateService.supports(pillar3)
    }
}
