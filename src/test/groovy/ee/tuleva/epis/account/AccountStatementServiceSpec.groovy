package ee.tuleva.epis.account

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.EpisX14Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class AccountStatementServiceSpec extends Specification {

    EpisService episService = Mock(EpisService);
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore);
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    AccountStatementService service = new AccountStatementService(episService,
            episMessageResponseStore, episMessageWrapper)

    def "Get account statement"() {
        given:
        String personalCode = "38080808080"
        EpisX14Type sampleResponse = new EpisX14Type()

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        });

        episMessageResponseStore.pop(_, EpisX14Type.class) >> sampleResponse

        when:
        EpisX14Type response = service.get(personalCode)

        then:
        response == sampleResponse

    }

}
