package ee.tuleva.onboarding.mandate.application

import ee.tuleva.onboarding.epis.EpisService
import spock.lang.Specification

class MandateApplicationListServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    MandateApplicationListService mandateApplicationListService = new MandateApplicationListService(episService)

    def "Get: Get list of mandate applications"() {
        given:
        String samplePersonalCode = "38012121212"

        when:
        List<MandateApplicationResponse> applications = mandateApplicationListService.get(samplePersonalCode)

        then:
        applications != null

    }
}
