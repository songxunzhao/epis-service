package ee.tuleva.epis.epis

import mhub.xsd.envelope._01.Ex
import spock.lang.Specification

import javax.xml.bind.JAXBElement

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.*

class EpisMessageWrapperSpec extends Specification {

    def soapEnvelopeFactory = new SoapEnvelopeFactory()
    def xRoadFactory = new XRoadFactory()
    def headFactory = new EnvelopeHeadFactory()
    def mhubEnvelopeFactory = new MhubEnvelopeFactory()

    EpisMessageWrapper service = new EpisMessageWrapper(soapEnvelopeFactory, xRoadFactory, headFactory, mhubEnvelopeFactory)

    def "wraps a message"() {
        given:
        String sampleId = 'sampleId'
        JAXBElement input = Mock(JAXBElement)
        when:
        Ex ex = service.wrap(sampleId, input)

        then:
        String headerContent = ((JAXBElement)ex.getBizMsg().envelope.header.any.get(0)).value
        String headerId = ex.bizMsg.appHdr.bizMsgIdr.toString()
        String messageId = ((JAXBElement)ex.bizMsg.envelope.header.any.get(1)).value

        String from = ex.bizMsg.appHdr.fr.fiId.finInstnId.bicfi
        String to = ex.bizMsg.appHdr.to.fiId.finInstnId.bicfi
        String messageDef =  ex.bizMsg.appHdr.msgDefIdr

        headerContent == "XMLTULEVA"
        headerId == messageId && headerId != null & headerId != ""
        from == "TULEVA20PPP"
        to == "LCDELV22XXX"
        messageDef == "epis"
    }
}
