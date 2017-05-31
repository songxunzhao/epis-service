package ee.tuleva.epis.epis.response.application.list

import com.google.common.collect.Lists
import ee.tuleva.epis.gen.ApplicationStatusType
import ee.tuleva.epis.gen.ApplicationType
import ee.tuleva.epis.gen.ApplicationTypeType
import ee.tuleva.epis.gen.ExchangeApplicationType
import ee.tuleva.epis.mandate.application.MandateExchangeApplicationResponse
import ee.tuleva.epis.mandate.application.MandateApplicationStatus
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory

class EpisApplicationListToMandateExchangeApplicationResponseListConverterSpec extends Specification {

    EpisApplicationListToMandateApplicationResponseListConverter converter =
            new EpisApplicationListToMandateApplicationResponseListConverter()

    def "Convert"() {
        given:
        List<ApplicationType> sampleEpisApplicationList = Lists.asList(
                sampleExchangeApplicationType(),
                sampleDiscardedApplicationType(),
                sampleEmptyExchangeApplicationType()
        )

        when:
        List<MandateExchangeApplicationResponse> results = converter.convert(sampleEpisApplicationList)
        then:
        results.size() == 2
        results.first().status == MandateApplicationStatus.COMPLETE
        results.first().date != null
        results.first().sourceFundIsin == sampleExchangeApplicationType().sourceISIN

        results.first().targetFundIsin ==
                sampleExchangeApplicationRows().exchangeApplicationRow.first().destinationISIN

        results.last().status == MandateApplicationStatus.COMPLETE
        results.last().date != null
        results.last().sourceFundIsin == sampleExchangeApplicationType().sourceISIN
        results.last().targetFundIsin ==
                sampleExchangeApplicationRows().exchangeApplicationRow.last().destinationISIN

    }

    ExchangeApplicationType sampleEmptyExchangeApplicationType() {

        ApplicationType.ApplicationData applicationData = new ApplicationType.ApplicationData();
        applicationData.applicationType = ApplicationTypeType.PEVA
        applicationData.status = ApplicationStatusType.R
        applicationData.documentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar()

        ExchangeApplicationType applicationType = new ExchangeApplicationType()
        applicationType.setApplicationData(
                applicationData
        )

        applicationType.setSourceISIN("sampleSourceIsin")
        applicationType.setExchangeApplicationRows(null)

        return applicationType
    }

    ExchangeApplicationType sampleExchangeApplicationType() {

        ApplicationType.ApplicationData applicationData = new ApplicationType.ApplicationData();
        applicationData.applicationType = ApplicationTypeType.PEVA
        applicationData.status = ApplicationStatusType.R
        applicationData.documentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar()

        ExchangeApplicationType applicationType = new ExchangeApplicationType()
        applicationType.setApplicationData(
                applicationData
        )

        applicationType.setSourceISIN("sampleSourceIsin")
        applicationType.setExchangeApplicationRows(sampleExchangeApplicationRows())

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

    ExchangeApplicationType.ExchangeApplicationRows sampleExchangeApplicationRows() {
        ExchangeApplicationType.ExchangeApplicationRows.ExchangeApplicationRow row1 =
                new ExchangeApplicationType.ExchangeApplicationRows.ExchangeApplicationRow()
        row1.destinationISIN = "sampleDestinationIsin1"
        row1.percentage = new BigDecimal(70)

        ExchangeApplicationType.ExchangeApplicationRows.ExchangeApplicationRow row2 =
                new ExchangeApplicationType.ExchangeApplicationRows.ExchangeApplicationRow()
        row2.destinationISIN = "sampleDestinationIsin2"
        row2.percentage = new BigDecimal(30)

        ExchangeApplicationType.ExchangeApplicationRows rows =
                new ExchangeApplicationType.ExchangeApplicationRows()
        rows.exchangeApplicationRow = Arrays.asList(row1, row2)

        return rows
    }

}
