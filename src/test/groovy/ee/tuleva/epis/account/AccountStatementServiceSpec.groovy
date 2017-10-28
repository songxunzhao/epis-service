package ee.tuleva.epis.account

import ee.tuleva.epis.contact.ContactDetails
import ee.tuleva.epis.contact.ContactDetailsService
import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.EpisX14Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class AccountStatementServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    ContactDetailsService contactDetailsService = Mock(ContactDetailsService)
    EpisX14TypeToFundBalanceListConverter converter = Mock(EpisX14TypeToFundBalanceListConverter)

    AccountStatementService service = new AccountStatementService(episService,
            episMessageResponseStore, episMessageWrapper, contactDetailsService, converter)

    def "Get account statement"() {
        given:
        String personalCode = "38080808080"
        EpisX14Type sampleResponse = new EpisX14Type()

        String sampleActiveIsin = 'sampleActiveIsin'

        List<FundBalance> sampleFundBalances = [
                new FundBalance('isin1', new BigDecimal(1), 'EUR', 2, false),
                new FundBalance(sampleActiveIsin, new BigDecimal(1), 'EUR', 2, false)
        ]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        });

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        1 * converter.convert(sampleResponse) >> sampleFundBalances

        1 * contactDetailsService.get(personalCode) >> ContactDetails.builder()
                .activeSecondPillarFundIsin(sampleActiveIsin).build()

        when:
        List<FundBalance> response = service.get(personalCode)

        then:
        response.size() == 2
        response.last().isin == sampleActiveIsin
        response.last().isActiveContributions()
        !response.first().isActiveContributions()
        response.first().isin == sampleFundBalances.first().isin

    }

}
