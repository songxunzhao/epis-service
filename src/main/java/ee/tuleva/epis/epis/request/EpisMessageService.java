package ee.tuleva.epis.epis.request;

import ee.tuleva.epis.epis.EpisMessageType;
import ee.tuleva.epis.mandate.application.MandateApplicationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisMessageService {

    @Value("${epis.service.bic}")
    private String episServiceBic;

    public EpisMessage get(EpisMessageType type, String message) {
        log.info("Wraping message with hasCode {}", message.hashCode());

        EpisMessage episMessage = getOuterLayer(
                getInnerLayer(
                        message
                )
        );

        episMessage.setType(type);

        return episMessage;
    }

    private String getInnerLayer(String content) {
        String transactionId = UUID.randomUUID().toString();

        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"http://epis.x-road.ee/producer/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\" xmlns:x=\"http://x-road.ee/xsd/x-road.xsd\">\n" +
                "  <soapenv:Header>\n" +
                "    <x:consumer xmlns=\"http://epis.x-road.ee/producer/\" xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:x=\"http://x-road.ee/xsd/x-road.xsd\">XMLTULEVA</x:consumer>\n" +
                "    <x:id xmlns=\"http://epis.x-road.ee/producer/\" xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:x=\"http://x-road.ee/xsd/x-road.xsd\">" + transactionId + "</x:id>\n" +
                "  </soapenv:Header>\n" +
                "  <soapenv:Body>\n" +
                "    " + content + "\n" +
                "  </soapenv:Body>\n" +
                "</soapenv:Envelope>\n";
    }

    private EpisMessage getOuterLayer(String content) {
        String id = UUID.randomUUID().toString().replace("-", "");
        log.info("Wrapping message with id {}", id);
        String message = episEnvelopePrefix(id) +
                content +
                episEnvelopeSuffix;


        return EpisMessage.builder().
                content(
                        message
                )
                .id(id)
                .build();
    }

    private MandateApplicationType getType(String xmlContent) {
        MandateApplicationType type = null;

        if(xmlContent.contains("<OSAKUTE_VAHETAMISE_AVALDUS>")) {
            type = MandateApplicationType.TRANSFER;
        } else if(xmlContent.contains("VALIKUAVALDUS")) {
            type = MandateApplicationType.SELECTION;
        } else {
            throw new RuntimeException("Unknown mandate xml message type");
        }

        return type;
    }

    private String senderBic = "TULEVA20";

    private String episEnvelopePrefix(String id)  {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<Ex xmlns=\"urn:mhub:xsd:Envelope:01\" xmlns:ns2=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\">\n" +
                "                <BizMsg>\n" +
                "                                <ns2:AppHdr>\n" +
                "                                                <ns2:Fr>\n" +
                "                                                                <ns2:FIId>\n" +
                "                                                                                <ns2:FinInstnId>\n" +
                "                                                                                                <ns2:BICFI>"+ senderBic +"</ns2:BICFI>\n" +
                "                                                                                </ns2:FinInstnId>\n" +
                "                                                                </ns2:FIId>\n" +
                "                                                </ns2:Fr>\n" +
                "                                                <ns2:To>\n" +
                "                                                                <ns2:FIId>\n" +
                "                                                                                <ns2:FinInstnId>\n" +
                "                                                                                                <ns2:BICFI>" + episServiceBic + "</ns2:BICFI>\n" +
                "                                                                                </ns2:FinInstnId>\n" +
                "                                                                </ns2:FIId>\n" +
                "                                                </ns2:To>\n" +
                "                                                <ns2:BizMsgIdr>"+id+"</ns2:BizMsgIdr>\n" +
                "                                                <ns2:MsgDefIdr>epis</ns2:MsgDefIdr>\n" +
                "                                                <ns2:CreDt>2017-03-13T10:00:39.731Z</ns2:CreDt>\n" +
                "                                </ns2:AppHdr>";
    }


    private String episEnvelopeSuffix = "                </BizMsg>\n" +
            "</Ex>";

}
