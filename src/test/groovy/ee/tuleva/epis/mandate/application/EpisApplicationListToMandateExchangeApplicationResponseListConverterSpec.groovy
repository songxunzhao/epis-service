package ee.tuleva.epis.mandate.application

import ee.tuleva.epis.mandate.application.list.EpisApplicationListToMandateApplicationResponseListConverter
import ee.x_road.epis.producer.ApplicationType
import ee.x_road.epis.producer.ExchangeApplicationType
import ee.x_road.epis.producer.SwitchApplicationType
import spock.lang.Specification

import javax.xml.datatype.DatatypeFactory

import static ee.tuleva.epis.mandate.application.MandateApplicationStatus.COMPLETE
import static ee.x_road.epis.producer.ApplicationStatusType.A
import static ee.x_road.epis.producer.ApplicationStatusType.R
import static ee.x_road.epis.producer.ApplicationType.ApplicationData
import static ee.x_road.epis.producer.ApplicationTypeType.*
import static ee.x_road.epis.producer.ExchangeApplicationType.ExchangeApplicationRows
import static ee.x_road.epis.producer.ExchangeApplicationType.ExchangeApplicationRows.ExchangeApplicationRow
import static ee.x_road.epis.producer.SwitchApplicationType.*
import static ee.x_road.epis.producer.SwitchApplicationType.ApplicationRows.ApplicationRow

class EpisApplicationListToMandateExchangeApplicationResponseListConverterSpec extends Specification {

    def converter = new EpisApplicationListToMandateApplicationResponseListConverter()

    def "converts"() {
        given:
        List<ApplicationType> sampleEpisApplicationList = [
            sampleExchangeApplication(),
            sampleDiscardedApplication(),
            sampleEmptyExchangeApplication(),
            sample3rdPillarSwitchApplication(),
        ]

        when:
        List<MandateExchangeApplicationResponse> results = converter.convert(sampleEpisApplicationList)

        then:
        results.size() == 4

        with(results[0]) {
            status == COMPLETE
            date != null
            sourceFundIsin == sampleExchangeApplication().sourceISIN
            targetFundIsin == sampleExchangeApplicationRows().exchangeApplicationRow.first().destinationISIN
            amount == sampleExchangeApplicationRows().exchangeApplicationRow.first().percentage * 0.01
        }
        with(results[1]) {
            status == COMPLETE
            date != null
            sourceFundIsin == sampleExchangeApplication().sourceISIN
            targetFundIsin == sampleExchangeApplicationRows().exchangeApplicationRow.last().destinationISIN
            amount == sampleExchangeApplicationRows().exchangeApplicationRow.last().percentage * 0.01
        }
        with(results[2]) {
            status == COMPLETE
            date != null
            sourceFundIsin == sample3rdPillarSwitchApplication().sourceISIN
            targetFundIsin == sample3rdPillarExchangeApplicationRows().applicationRow.first().ISIN
            amount == sample3rdPillarExchangeApplicationRows().applicationRow.first().unitAmount
        }
        with(results[3]) {
            status == COMPLETE
            date != null
            sourceFundIsin == sample3rdPillarSwitchApplication().sourceISIN
            targetFundIsin == sample3rdPillarExchangeApplicationRows().applicationRow.last().ISIN
            amount == sample3rdPillarExchangeApplicationRows().applicationRow.last().unitAmount
        }
    }

    ExchangeApplicationType sampleEmptyExchangeApplication() {
        def data = new ApplicationData()
        data.applicationType = PEVA
        data.status = R
        data.documentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar()

        def application = new ExchangeApplicationType()
        application.setApplicationData(data)
        application.setSourceISIN("sampleSourceIsin")
        application.setExchangeApplicationRows(null)

        return application
    }

    ExchangeApplicationType sampleExchangeApplication() {
        def data = new ApplicationData()
        data.applicationType = PEVA
        data.status = R
        data.documentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar()

        def application = new ExchangeApplicationType()
        application.setApplicationData(data)
        application.setSourceISIN("sampleSourceIsin")
        application.setExchangeApplicationRows(sampleExchangeApplicationRows())

        return application
    }

    ApplicationType sampleDiscardedApplication() {
        def data = new ApplicationData()
        data.applicationType = FPLA
        data.status = A

        def application = new ApplicationType()
        application.setApplicationData(data)

        return application
    }

    SwitchApplicationType sample3rdPillarSwitchApplication() {
        def data = new ApplicationData()
        data.applicationType = SWI
        data.status = R
        data.documentDate = DatatypeFactory.newInstance().newXMLGregorianCalendar()

        def application = new SwitchApplicationType()
        application.setApplicationData(data)
        application.setSourceISIN("sampleSourceIsin2")
        application.setApplicationRows(sample3rdPillarExchangeApplicationRows())

        return application
    }

    ExchangeApplicationRows sampleExchangeApplicationRows() {
        def row1 = new ExchangeApplicationRow()
        row1.destinationISIN = "sampleDestinationIsin1"
        row1.percentage = 70.0

        def row2 = new ExchangeApplicationRow()
        row2.destinationISIN = "sampleDestinationIsin2"
        row2.percentage = 30.0

        def rows = new ExchangeApplicationRows()
        rows.exchangeApplicationRow.addAll(row1, row2)

        return rows
    }

    ApplicationRows sample3rdPillarExchangeApplicationRows() {
        def row1 = new ApplicationRow()
        row1.ISIN = "sampleDestinationIsin3"
        row1.unitAmount = 1500.0

        def row2 = new ApplicationRow()
        row2.ISIN = "sampleDestinationIsin4"
        row2.unitAmount = 1200.0

        def rows = new ApplicationRows()
        rows.applicationRow.addAll(row1, row2)

        return rows
    }

}
