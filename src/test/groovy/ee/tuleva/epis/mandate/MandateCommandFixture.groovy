package ee.tuleva.epis.mandate


import java.time.Instant

import static ee.tuleva.epis.mandate.MandateCommand.MandateCommandBuilder
import static ee.tuleva.epis.mandate.MandateCommand.builder
import static ee.tuleva.epis.mandate.application.FundTransferExchangeFixture.fundTransferExchangeFixture

class MandateCommandFixture {

    static MandateCommandBuilder mandateCommandFixture() {
        return builder()
            .id(123L)
            .pillar(2)
            .createdDate(Instant.now())
            .processId(UUID.randomUUID().toString().replace("-", ""))
            .futureContributionFundIsin("EE3600109435")
            .fundTransferExchanges([fundTransferExchangeFixture().build()])
    }
}
