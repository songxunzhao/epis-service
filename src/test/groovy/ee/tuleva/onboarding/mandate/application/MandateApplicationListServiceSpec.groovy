package ee.tuleva.onboarding.mandate.application

import ee.tuleva.onboarding.epis.request.EpisMessage
import ee.tuleva.onboarding.epis.request.EpisMessageService
import ee.tuleva.onboarding.epis.EpisMessageType
import ee.tuleva.onboarding.epis.EpisService
import ee.tuleva.onboarding.epis.response.EpisMessageResponseStore
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
        1 * episMessageService.get(
                EpisMessageType.LIST_APPLICATIONS,
                sampleEpisListMessageContent
        ) >> sampleEpisMessage
        when:
        List<MandateApplicationResponse> applications = mandateApplicationListService.get(samplePersonalCode)

        then:
        applications == null

    }

}
