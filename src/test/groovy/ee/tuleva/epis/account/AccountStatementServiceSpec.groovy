package ee.tuleva.epis.account

import ee.tuleva.epis.contact.ContactDetails
import ee.tuleva.epis.contact.ContactDetailsService
import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.converter.EpisX14TypeToCashFlowStatementConverter
import ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalancesConverter
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.fund.Fund
import ee.tuleva.epis.fund.FundService
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.EpisX14Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement
import java.time.Instant
import java.time.LocalDate

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.fund.Fund.FundStatus.ACTIVE
import static java.math.BigDecimal.ONE

class AccountStatementServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    ContactDetailsService contactDetailsService = Mock(ContactDetailsService)
    EpisX14TypeToFundBalancesConverter toFundBalancesConverter = Mock(EpisX14TypeToFundBalancesConverter)
    EpisMessageFactory episMessageFactory = new EpisMessageFactory()
    EpisX14TypeToCashFlowStatementConverter toCashFlowStatementConverter = Mock(EpisX14TypeToCashFlowStatementConverter)
    FundService fundService = Mock(FundService)

    AccountStatementService service = new AccountStatementService(
        episService,
        episMessageResponseStore,
        episMessageWrapper,
        contactDetailsService,
        toFundBalancesConverter,
        toCashFlowStatementConverter,
        episMessageFactory,
        fundService
    )

    def "Can get an account statement"() {
        given:
        String personalCode = "38080808080"
        EpisX14Type sampleResponse = new EpisX14Type()

        String sampleActiveIsin = "EE3600109435"
        Integer samplePillar = 2

        List<FundBalance> sampleFundBalances = [
            FundBalance.builder()
                .isin('isin1').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            FundBalance.builder()
                .isin(sampleActiveIsin).value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        } as JAXBElement)

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * toFundBalancesConverter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.get(personalCode) >> ContactDetails.builder()
            .activeSecondPillarFundIsin(sampleActiveIsin).build()

        1 * fundService.getPensionFunds() >> [new Fund(sampleActiveIsin, "Fund Name", "TUK75", samplePillar, ACTIVE)]

        when:
        List<FundBalance> response = service.getAccountStatement(personalCode)

        then:
        response.size() == 2
        !response.first().isActiveContributions()
        response.first().isin == sampleFundBalances.first().isin
        response.first().pillar == null
        response.last().isin == sampleActiveIsin
        response.last().isActiveContributions()
        response.last().pillar == samplePillar
    }

    def "Add new balance when active is not yet present"() {
        given:
        String personalCode = "38080808080"
        EpisX14Type sampleResponse = new EpisX14Type()

        String sampleActiveIsin = 'sampleActiveIsin'

        List<FundBalance> sampleFundBalances = [
            FundBalance.builder()
                .isin('isin1').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            FundBalance.builder()
                .isin('isin2').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        } as JAXBElement);

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * toFundBalancesConverter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.get(personalCode) >> ContactDetails.builder()
            .activeSecondPillarFundIsin(sampleActiveIsin).build()

        1 * fundService.getPensionFunds() >> []

        when:
        List<FundBalance> response = service.getAccountStatement(personalCode)

        then:
        response.size() == 3
        response.last().isin == sampleActiveIsin
        response.last().isActiveContributions()
        !response.first().isActiveContributions()
        response.first().isin == sampleFundBalances.first().isin
        !response.get(1).isActiveContributions()
        response.get(1).isin == sampleFundBalances.get(1).isin
        response.get(1).pillar == null
    }

    def "Can get a cashflow statement"() {
        given:
        String personalCode = "38080808080"
        def startDate = LocalDate.of(2003,  1,  7)
        def endDate = LocalDate.of(2018, 6, 15)
        EpisX14Type sampleResponse = new EpisX14Type()

        String sampleActiveIsin = "EE3600109435"
        Integer samplePillar = 2

        def balance = Transaction.builder()
            .time(Instant.now())
            .pillar(null)
            .currency("EUR")
            .amount(new BigDecimal("1.23"))
            .build()

        def transaction = Transaction.builder()
            .time(Instant.now())
            .pillar(samplePillar)
            .currency("EUR")
            .amount(new BigDecimal("1.23"))
            .build()

        def sampleCashFlowStatement = CashFlowStatement.builder()
            .startBalance([(sampleActiveIsin): balance])
            .endBalance([(sampleActiveIsin): balance])
            .transactions([transaction])
            .build()

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        } as JAXBElement)

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * toCashFlowStatementConverter.convert(sampleResponse) >> sampleCashFlowStatement

        2 * fundService.getPensionFunds() >> [new Fund(sampleActiveIsin, "Fund Name", "TUK75", samplePillar, ACTIVE)]

        when:
        CashFlowStatement cashFlowStatement = service.getCashFlowStatement(personalCode, startDate, endDate)

        then:
        cashFlowStatement.startBalance.get(sampleActiveIsin).pillar == samplePillar
        cashFlowStatement.endBalance.get(sampleActiveIsin).pillar == samplePillar
        cashFlowStatement.transactions == sampleCashFlowStatement.transactions
    }


}
