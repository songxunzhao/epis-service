package ee.tuleva.epis.account

import ee.tuleva.epis.config.UserPrincipal
import ee.tuleva.epis.contact.ContactDetails
import ee.tuleva.epis.contact.ContactDetailsService
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.converter.EpisX14TypeToCashFlowStatementConverter
import ee.tuleva.epis.epis.converter.EpisX14TypeToFundBalancesConverter
import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter
import ee.tuleva.epis.epis.request.EpisMessageWrapper
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.fund.Fund
import ee.tuleva.epis.fund.FundService
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.EpisX14Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement
import java.time.LocalDate

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.config.UserPrincipalFixture.userPrincipalFixture
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
    LocalDateToXmlGregorianCalendarConverter dateConverter = new LocalDateToXmlGregorianCalendarConverter()

    AccountStatementService service = new AccountStatementService(
        episService,
        episMessageResponseStore,
        episMessageWrapper,
        contactDetailsService,
        toFundBalancesConverter,
        toCashFlowStatementConverter,
        episMessageFactory,
        fundService,
        dateConverter
    )

    def "Can get an account statement"() {
        given:
        UserPrincipal principal = userPrincipalFixture()
        EpisX14Type sampleResponse = new EpisX14Type()

        String secondPillarActiveIsin = "EE3600109435"
        String thirdPillarActiveIsin = "EE234"
        Integer secondPillar = 2

        List<FundBalance> sampleFundBalances = [
            FundBalance.builder()
                .isin('isin1').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            FundBalance.builder()
                .isin(secondPillarActiveIsin).value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            FundBalance.builder()
                .isin(thirdPillarActiveIsin).value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == principal.personalCode
        } as JAXBElement)

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * toFundBalancesConverter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.getContactDetails(principal) >>
            ContactDetails.builder()
                .activeSecondPillarFundIsin(secondPillarActiveIsin)
                .thirdPillarDistribution([new ContactDetails.Distribution(thirdPillarActiveIsin, 1.0)])
                .build()

        1 * fundService.getPensionFunds() >> [new Fund(secondPillarActiveIsin, "Fund Name", "TUK75", secondPillar, ACTIVE)]

        when:
        List<FundBalance> response = service.getAccountStatement(principal)

        then:
        response.size() == 3

        with(response.get(0)) {
            isin == sampleFundBalances.first().isin
            !isActiveContributions()
            pillar == null
        }

        with(response.get(1)) {
            isin == secondPillarActiveIsin
            isActiveContributions()
            pillar == secondPillar
        }

        with(response.get(2)) {
            isin == thirdPillarActiveIsin
            isActiveContributions()
            pillar == null
        }


    }

    def "Add new balance when active is not yet present"() {
        given:
        UserPrincipal principal = userPrincipalFixture()
        EpisX14Type sampleResponse = new EpisX14Type()

        String secondPillarActiveIsin = 'EE123'
        String thirdPillarActiveIsin = 'EE234'

        List<FundBalance> sampleFundBalances = [
            FundBalance.builder()
                .isin('isin1').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
            FundBalance.builder()
                .isin('isin2').value(ONE).currency('EUR').pillar(null).activeContributions(false).build(),
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == principal.personalCode
        } as JAXBElement);

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * toFundBalancesConverter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.getContactDetails(principal) >>
            ContactDetails.builder()
                .activeSecondPillarFundIsin(secondPillarActiveIsin)
                .thirdPillarDistribution([new ContactDetails.Distribution(thirdPillarActiveIsin, 1.0)])
                .build()

        1 * fundService.getPensionFunds() >> []

        when:
        List<FundBalance> response = service.getAccountStatement(principal)

        then:
        response.size() == 4

        with(response.get(0)) {
            isin == sampleFundBalances.first().isin
            !isActiveContributions()
        }

        with(response.get(1)) {
            isin == sampleFundBalances.get(1).isin
            !isActiveContributions()
        }

        with(response.get(2)) {
            isin == secondPillarActiveIsin
            isActiveContributions()
        }

        with(response.get(3)) {
            isin == thirdPillarActiveIsin
            isActiveContributions()
        }
    }

    def "Can get a cashflow statement"() {
        given:
        UserPrincipal principal = userPrincipalFixture()
        def startDate = LocalDate.of(2003, 1, 7)
        def endDate = LocalDate.of(2018, 6, 15)
        EpisX14Type sampleResponse = new EpisX14Type()

        String sampleActiveIsin = "EE3600109435"

        def balance = Transaction.builder()
            .date(LocalDate.now())
            .amount(new BigDecimal("1.23"))
            .build()

        def transaction = Transaction.builder()
            .date(LocalDate.now())
            .amount(new BigDecimal("1.23"))
            .build()

        def sampleCashFlowStatement = CashFlowStatement.builder()
            .startBalance([(sampleActiveIsin): balance])
            .endBalance([(sampleActiveIsin): balance])
            .transactions([transaction])
            .build()

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == principal.personalCode
        } as JAXBElement)

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * toCashFlowStatementConverter.convert(sampleResponse) >> sampleCashFlowStatement


        when:
        CashFlowStatement cashFlowStatement = service.getCashFlowStatement(principal.personalCode, startDate, endDate)

        then:
        cashFlowStatement.transactions == sampleCashFlowStatement.transactions
    }


}
