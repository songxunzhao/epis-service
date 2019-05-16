package ee.tuleva.epis.mandate

import ee.tuleva.epis.mandate.application.FundTransferExchange

import java.time.Instant

class MandateCommandFixture {

    static MandateCommand mandateCommandFixture() {
        return MandateCommand.builder()
            .id(123L)
            .pillar(2)
            .createdDate(Instant.now())
            .processId("gfdsa")
            .futureContributionFundIsin("EE3600109435")
            .fundTransferExchanges([
                FundTransferExchange.builder()
                    .processId("asdfg")
                    .amount(1.0)
                    .sourceFundIsin("EE3600019832")
                    .targetFundIsin("EE3600109435")
                    .build()
            ])
            .build()
    }
}
