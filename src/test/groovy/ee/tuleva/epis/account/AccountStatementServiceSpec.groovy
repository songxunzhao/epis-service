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

import static ee.tuleva.epis.account.FundBalance.*
import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.fund.Fund.FundStatus.ACTIVE
import static java.math.BigDecimal.ONE

class AccountStatementServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    ContactDetailsService contactDetailsService = Mock(ContactDetailsService)
    EpisX14TypeToFundBalancesConverter converter = Mock(EpisX14TypeToFundBalancesConverter)
    EpisMessageFactory episMessageFactory = new EpisMessageFactory()
    EpisX14TypeToCashFlowStatementConverter toCashFlowStatementConverter = Mock(EpisX14TypeToCashFlowStatementConverter)
    FundService fundService = Mock(FundService)

    AccountStatementService service = new AccountStatementService(
        episService,
        episMessageResponseStore,
        episMessageWrapper,
        contactDetailsService,
        converter,
        toCashFlowStatementConverter,
        episMessageFactory,
        fundService
    )

    def "Get account statement"() {
        given:
        String personalCode = "38080808080"
        EpisX14Type sampleResponse = new EpisX14Type()

        String sampleActiveIsin = "EE3600109435"
        Integer samplePillar = 2

        List<FundBalance> sampleFundBalances = [
            new FundBalanceBuilder()
                .isin('isin1').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            new FundBalanceBuilder()
                .isin(sampleActiveIsin).value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        } as JAXBElement);

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * converter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.get(personalCode) >> ContactDetails.builder()
            .activeSecondPillarFundIsin(sampleActiveIsin).build()

        1 * fundService.getPensionFunds() >> [new Fund(sampleActiveIsin, "Fund Name", "TUK75", samplePillar, ACTIVE)]

        when:
        List<FundBalance> response = service.get(personalCode)

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
            new FundBalanceBuilder()
                .isin('isin1').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            new FundBalanceBuilder()
                .isin('isin2').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        } as JAXBElement);

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * converter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.get(personalCode) >> ContactDetails.builder()
            .activeSecondPillarFundIsin(sampleActiveIsin).build()

        1 * fundService.getPensionFunds() >> []

        when:
        List<FundBalance> response = service.get(personalCode)

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


}
