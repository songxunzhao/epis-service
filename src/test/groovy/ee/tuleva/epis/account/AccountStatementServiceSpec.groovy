package ee.tuleva.epis.account

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.person.PersonService
import ee.x_road.epis.producer.EpisX12Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class AccountStatementServiceSpec extends Specification {

    EpisService episService = Mock(EpisService);
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore);
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    PersonService service = new PersonService(episService,
            episMessageResponseStore, episMessageWrapper)

    def "Get account statement"() {
        given:
        String personalCode = "38080808080"
        EpisX12Type sampleResponse = new EpisX12Type()

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX12Type> personalDataRequest ->

            def requestPersonalCode = personalDataRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        });

        episMessageResponseStore.pop(_, EpisX12Type.class) >> sampleResponse

        when:
        EpisX12Type response = service.get(personalCode)

        then:
        response == sampleResponse

    }

}
