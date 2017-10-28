package ee.tuleva.epis.mandate.application

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.request.EpisMessageService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.EpisX26Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class MandateApplicationListServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageService episMessageService = Mock(EpisMessageService)
    MandateApplicationListMessageCreatorService mandateApplicationListMessageCreatorService =
            Mock(MandateApplicationListMessageCreatorService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper);

    MandateApplicationListService service =
            new MandateApplicationListService(
                    episService, episMessageResponseStore, episMessageWrapper)

    def "Get: Get list of mandate applications"() {
        given:
        String personalCode = "38080808080"
        List<EpisX26Type> sampleResponse = [new EpisX26Type()]

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX26Type> applicationListRequest ->

            def requestPersonalCode = applicationListRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        });

        episMessageResponseStore.pop(_, List.class) >> sampleResponse

        when:
        List<EpisX12Type> response = service.get(personalCode)

        then:
        response == sampleResponse

    }

}
