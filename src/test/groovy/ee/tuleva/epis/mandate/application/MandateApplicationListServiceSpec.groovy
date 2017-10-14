package ee.tuleva.epis.mandate.application

import ee.tuleva.epis.epis.request.EpisMessage
import ee.tuleva.epis.epis.request.EpisMessageService
import ee.tuleva.epis.epis.EpisMessageType
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import spock.lang.Specification

class MandateApplicationListServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageService episMessageService = Mock(EpisMessageService)
    MandateApplicationListMessageCreatorService mandateApplicationListMessageCreatorService =
            Mock(MandateApplicationListMessageCreatorService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)

    MandateApplicationListService mandateApplicationListService =
            new MandateApplicationListService(
                    episService, episMessageService, mandateApplicationListMessageCreatorService,
                    episMessageResponseStore)

    def "Get: Get list of mandate applications"() {
        given:
        String samplePersonalCode = "38012121212"
        String sampleEpisListMessageContent = "<xml>${samplePersonalCode}</xml>"

        EpisMessage sampleEpisMessage = EpisMessage.builder()
                .id("123")
                .content("message content")
                .build()

        1 * mandateApplicationListMessageCreatorService.getMessage(samplePersonalCode) >> sampleEpisListMessageContent
        episMessageResponseStore.pop(sampleEpisMessage.getId(), List.class) >> [1]
        1 * episMessageService.get(
                EpisMessageType.LIST_APPLICATIONS,
                sampleEpisListMessageContent
        ) >> sampleEpisMessage
        when:
        List<MandateExchangeApplicationResponse> applications = mandateApplicationListService.get(samplePersonalCode)

        then:
        applications.size() == 1

    }
}
