package ee.tuleva.epis.mandate

import static ee.tuleva.epis.mandate.MandateResponse.*
import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION

class MandateResponseFixture {

    static MandateResponseBuilder mandateResponseFixture() {
        return builder()
            .processId(UUID.randomUUID().toString().replace("-", ""))
            .applicationType(SELECTION)
            .successful(true)
            .errorMessage(null)
            .errorCode(null)
    }
}
