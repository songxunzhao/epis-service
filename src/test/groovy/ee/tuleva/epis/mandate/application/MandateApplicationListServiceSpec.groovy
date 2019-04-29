package ee.tuleva.epis.mandate.application

import ee.tuleva.epis.epis.EpisMessageWrapper
import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.tuleva.epis.mandate.application.list.EpisApplicationListToMandateApplicationResponseListConverter
import ee.x_road.epis.producer.EpisX26ResponseType
import ee.x_road.epis.producer.EpisX26Type
import ee.x_road.epis.producer.ResultType
import spock.lang.Specification

import javax.xml.bind.JAXBElement

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory

class MandateApplicationListServiceSpec extends Specification {

    EpisService episService = Mock(EpisService)
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    EpisMessageWrapper episMessageWrapper = Mock(EpisMessageWrapper)
    EpisApplicationListToMandateApplicationResponseListConverter converter =
        Mock(EpisApplicationListToMandateApplicationResponseListConverter)
    EpisMessageFactory episMessageFactory = new EpisMessageFactory()

    MandateApplicationListService service =
            new MandateApplicationListService(
                    episService, episMessageResponseStore, episMessageWrapper, converter, episMessageFactory)

    def "Get: Get list of mandate applications"() {
        given:
        String personalCode = "38080808080"

        def goesToConversion = Mock(List)

        EpisX26ResponseType.Applications applications = Mock(EpisX26ResponseType.Applications
                ,{
            getApplicationOrExchangeApplicationOrFundPensionOpen() >> goesToConversion
        })

        ResultType resultType = new ResultType()
        EpisX26ResponseType episX26ResponseType = new EpisX26ResponseType()
        episX26ResponseType.setApplications(applications)
        episX26ResponseType.setResults(resultType)

        EpisX26Type episX26Type = new EpisX26Type()
        episX26Type.setResponse(episX26ResponseType)

        List<MandateExchangeApplicationResponse> sampleResponseList = []

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX26Type> applicationListRequest ->
            def requestPersonalCode = applicationListRequest.getValue().getRequest().getPersonalData().getPersonId()
            return requestPersonalCode == personalCode
        });

        1 * episMessageResponseStore.pop(_, EpisX26Type.class) >> episX26Type
        1 * converter.convert(goesToConversion) >> sampleResponseList


        when:
        List<MandateExchangeApplicationResponse> response = service.get(personalCode)

        then:
        response == sampleResponseList
    }

    def "Get: return empty list when result code is present"() {
        given:
        String personalCode = "38080808080"

        ResultType resultType = new ResultType()
        resultType.setResultCode(112233)
        EpisX26ResponseType episX26ResponseType = new EpisX26ResponseType()
        episX26ResponseType.setResults(resultType)
        EpisX26Type episX26Type = new EpisX26Type()
        episX26Type.setResponse(episX26ResponseType)

        1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX26Type> applicationListRequest ->
            def requestPersonalCode = applicationListRequest.getValue().getRequest().getPersonalData().getPersonId()
            return requestPersonalCode == personalCode
        })

        1 * episMessageResponseStore.pop(_, EpisX26Type.class) >> episX26Type

        when:
        List<MandateExchangeApplicationResponse> response = service.get(personalCode)

        then:
        response == []
    }

	def "Get: return empty list if no applications"() {
		given:
		String personalCode = "38080808080"

		ResultType resultType = new ResultType()
		EpisX26ResponseType episX26ResponseType = new EpisX26ResponseType()
		episX26ResponseType.setResults(resultType)
		EpisX26Type episX26Type = new EpisX26Type()
		episX26Type.setResponse(episX26ResponseType)

		1 * episMessageWrapper.wrap(_ as String, { JAXBElement<EpisX26Type> applicationListRequest ->
			def requestPersonalCode = applicationListRequest.getValue().getRequest().getPersonalData().getPersonId()
			return requestPersonalCode == personalCode
		})

		1 * episMessageResponseStore.pop(_, EpisX26Type.class) >> episX26Type

		when:
		List<MandateExchangeApplicationResponse> response = service.get(personalCode)

		then:
		response == []
	}
}
