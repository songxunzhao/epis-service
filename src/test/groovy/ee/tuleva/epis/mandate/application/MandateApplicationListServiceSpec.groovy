package ee.tuleva.epis.mandate.application

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.mandate.application.list.EpisApplicationListToMandateApplicationResponseListConverter
import ee.x_road.epis.producer.EpisX26ResponseType
import ee.x_road.epis.producer.EpisX26Type
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class MandateApplicationListServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    EpisApplicationListToMandateApplicationResponseListConverter converter =
            Mock(EpisApplicationListToMandateApplicationResponseListConverter)

    MandateApplicationListService service =
            new MandateApplicationListService(
                    episService, episMessageResponseStore, episMessageWrapper, converter)

    def "Get: Get list of mandate applications"() {
        given:
        String personalCode = "38080808080"

        def goesToConversion = Mock(List)

        EpisX26ResponseType.Applications applications = Mock(EpisX26ResponseType.Applications
                ,{
            getApplicationOrExchangeApplicationOrFundPensionOpen() >> goesToConversion
        })

        EpisX26ResponseType episX26ResponseType = new EpisX26ResponseType()
        episX26ResponseType.setApplications(applications)

        List<MandateExchangeApplicationResponse> sampleResponseList = []

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX26Type> applicationListRequest ->

            def requestPersonalCode = applicationListRequest.getValue().getRequest().getPersonalData().getPersonId()

            return requestPersonalCode == personalCode
        });

        1 * episMessageResponseStore.pop(_, EpisX26ResponseType.class) >> episX26ResponseType
        1 * converter.convert(goesToConversion) >> sampleResponseList


        when:
        List<MandateExchangeApplicationResponse> response = service.get(personalCode)

        then:
        response == sampleResponseList

    }

}
