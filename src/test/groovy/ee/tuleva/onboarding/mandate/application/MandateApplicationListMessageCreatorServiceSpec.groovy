package ee.tuleva.onboarding.mandate.application

import spock.lang.Specification

class MandateApplicationListMessageCreatorServiceSpec extends Specification {

    MandateApplicationListMessageCreatorService service = new MandateApplicationListMessageCreatorService()

    def "getMessage: Gets applications list message"() {
        given:
        String samplePersonalCode = "3012121212"

        when:
        String message = service.getMessage(samplePersonalCode)

        then:
        message == "<AVALDUSTE_LOETELU>\n" +
                "         <Request>\n" +
                "                       <PersonalData PersonId=\"" + samplePersonalCode + "\"/>\n" +
                "                                </Request>\n" +
                "      </AVALDUSTE_LOETELU>";
    }

}
