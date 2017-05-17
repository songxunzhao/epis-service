package ee.tuleva.onboarding.epis.response.application.list

import com.google.common.collect.Lists
import ee.tuleva.epis.gen.ApplicationStatusType
import ee.tuleva.epis.gen.ApplicationType
import ee.tuleva.epis.gen.ApplicationTypeType
import ee.tuleva.onboarding.mandate.application.MandateApplicationResponse
import ee.tuleva.onboarding.mandate.application.MandateApplicationStatus
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory

class EpisApplicationListToMandateApplicationResponseListConverterSpec extends Specification {

    EpisApplicationListToMandateApplicationResponseListConverter converter =
            new EpisApplicationListToMandateApplicationResponseListConverter()

    def "Convert"() {
        given:
        List<ApplicationType> sampleEpisApplicationList = Lists.asList(
                sampleApplicationType(),
                sampleDiscardedApplicationType()
        )

        when:
        List<MandateApplicationResponse> results = converter.convert(sampleEpisApplicationList)
        then:
        results.size() == 1
        MandateApplicationResponse mandateApplicationResponse = results.first()
        mandateApplicationResponse.status == MandateApplicationStatus.COMPLETE
        mandateApplicationResponse.date != null
    }

    ApplicationType sampleApplicationType() {

        ApplicationType.ApplicationData applicationData = new ApplicationType.ApplicationData();
        applicationData.applicationType = ApplicationTypeType.PEVA
        applicationData.status = ApplicationStatusType.A
        applicationData.documentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar()

        ApplicationType applicationType = new ApplicationType()
        applicationType.setApplicationData(
                applicationData
        )

        return applicationType
    }

    ApplicationType sampleDiscardedApplicationType() {

        ApplicationType.ApplicationData applicationData = new ApplicationType.ApplicationData();
        applicationData.applicationType = ApplicationTypeType.FPLA
        applicationData.status = ApplicationStatusType.A

        ApplicationType applicationType = new ApplicationType()
        applicationType.setApplicationData(
                applicationData
        )

        return applicationType
    }


}
