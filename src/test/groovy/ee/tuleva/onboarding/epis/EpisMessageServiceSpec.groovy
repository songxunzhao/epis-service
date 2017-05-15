package ee.tuleva.onboarding.epis

import spock.lang.Specification

class EpisMessageServiceSpec extends Specification {

    EpisMessageService service = new EpisMessageService()

    def "get: Wraps a message into Epis message wrapper"() {
        given:
        service.episServiceBic = "sampleBic";

        when:
        EpisMessage episMessage = service.get(EpisMessageType.LIST_APPLICATIONS, "message")
        then:
        episMessage.message.startsWith(messageBeforeRandomId(episMessage.id, service.episServiceBic))
        episMessage.message.endsWith(messageAfterRandomId)
    }


    private messageBeforeRandomId(String id, String bic) {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Ex xmlns="urn:mhub:xsd:Envelope:01" xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:head.001.001.01">
                <BizMsg>
                                <ns2:AppHdr>
                                                <ns2:Fr>
                                                                <ns2:FIId>
                                                                                <ns2:FinInstnId>
                                                                                                <ns2:BICFI>TULEVA20</ns2:BICFI>
                                                                                </ns2:FinInstnId>
                                                                </ns2:FIId>
                                                </ns2:Fr>
                                                <ns2:To>
                                                                <ns2:FIId>
                                                                                <ns2:FinInstnId>
                                                                                                <ns2:BICFI>${bic}</ns2:BICFI>
                                                                                </ns2:FinInstnId>
                                                                </ns2:FIId>
                                                </ns2:To>
                                                <ns2:BizMsgIdr>${id}</ns2:BizMsgIdr>
                                                <ns2:MsgDefIdr>epis</ns2:MsgDefIdr>
                                                <ns2:CreDt>2017-03-13T10:00:39.731Z</ns2:CreDt>
                                </ns2:AppHdr><soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns="http://epis.x-road.ee/producer/" xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:head.001.001.01" xmlns:x="http://x-road.ee/xsd/x-road.xsd">
  <soapenv:Header>
    <x:consumer xmlns="http://epis.x-road.ee/producer/" xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:head.001.001.01" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:x="http://x-road.ee/xsd/x-road.xsd">XMLTULEVA</x:consumer>
    <x:id xmlns="http://epis.x-road.ee/producer/" xmlns:ns2="urn:iso:std:iso:20022:tech:xsd:head.001.001.01" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:x="http://x-road.ee/xsd/x-road.xsd">"""

    }

    String messageAfterRandomId = """</x:id>
  </soapenv:Header>
  <soapenv:Body>
    message
  </soapenv:Body>
</soapenv:Envelope>
                </BizMsg>
</Ex>"""


}
