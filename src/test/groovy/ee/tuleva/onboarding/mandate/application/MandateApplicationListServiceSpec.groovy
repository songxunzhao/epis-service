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
        1 * episMessageResponseStore.pop(sampleEpisMessage.getId()) >> sampleStoredJson
        1 * episMessageService.get(
                EpisMessageType.LIST_APPLICATIONS,
                sampleEpisListMessageContent
        ) >> sampleEpisMessage
        when:
        List<MandateExchangeApplicationResponse> applications = mandateApplicationListService.get(samplePersonalCode)

        then:
        applications.size() == 1

    }

    String sampleStoredJson = """
    [
      {
        "currency": "EUR",
        "date": {
          "nano": 0,
          "epochSecond": 1491253200
        },
        "id": "2651881",
        "documentNumber": "210",
        "amount": 1,
        "status": "FAILED",
        "sourceFundIsin": "EE3600019832",
        "targetFundIsin": "EE3600109435"
      }]
    """

}
