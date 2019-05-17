package ee.tuleva.epis.mandate.application


import static ee.tuleva.epis.mandate.application.FundTransferExchange.FundTransferExchangeBuilder
import static ee.tuleva.epis.mandate.application.FundTransferExchange.builder

class FundTransferExchangeFixture {

    static FundTransferExchangeBuilder fundTransferExchangeFixture() {
        return builder()
            .processId(UUID.randomUUID().toString().replace("-", ""))
            .amount(1.0)
            .sourceFundIsin("EE3600019832")
            .targetFundIsin("EE3600109435")
    }

}
