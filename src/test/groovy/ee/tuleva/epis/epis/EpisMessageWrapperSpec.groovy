package ee.tuleva.epis.epis

import mhub.xsd.envelope._01.Ex
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class EpisMessageWrapperSpec extends Specification {
    def "Wrap"() {

        EpisMessageWrapper service = new EpisMessageWrapper()

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
        from == "TULEVA20"
        to == "ECSDEE20"
        messageDef == "epis"
    }
}
