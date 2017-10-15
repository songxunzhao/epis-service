package ee.tuleva.epis.epis;

import iso.std.iso._20022.tech.xsd.head_001_001.BranchAndFinancialInstitutionIdentification5;
import iso.std.iso._20022.tech.xsd.head_001_001.BusinessApplicationHeaderV01;
import iso.std.iso._20022.tech.xsd.head_001_001.FinancialInstitutionIdentification8;
import iso.std.iso._20022.tech.xsd.head_001_001.Party9Choice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import org.xmlsoap.schemas.soap.envelope.Header;
import org.xmlsoap.schemas.soap.envelope.ObjectFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

@Slf4j
@Component
@RequiredArgsConstructor
public class EpisMessageWrapper {

    @Value("${epis.service.bic}")
    private String episServiceBic;
    //TODO: replace with bean
    private ObjectFactory objectFactory = new ObjectFactory();

    public Ex wrap(String id, JAXBElement input) {
        ObjectFactory envelopeFactory = objectFactory;
        ee.x_road.xsd.x_road.ObjectFactory xRoadFactory = new ee.x_road.xsd.x_road.ObjectFactory();

        Header header = envelopeFactory.createHeader();
        JAXBElement<String> consumer = xRoadFactory.createConsumer("XMLTULEVA");
        JAXBElement<String> headerId = xRoadFactory.createId(id);
        header.getAny().add(consumer);
        header.getAny().add(headerId);

        iso.std.iso._20022.tech.xsd.head_001_001.ObjectFactory headFactory = new iso.std.iso._20022.tech.xsd.head_001_001.ObjectFactory();

        // FROM
        FinancialInstitutionIdentification8 fromFinInstnId = headFactory.createFinancialInstitutionIdentification8();
        fromFinInstnId.setBICFI("TULEVA20");

        BranchAndFinancialInstitutionIdentification5 fromFiId = headFactory.createBranchAndFinancialInstitutionIdentification5();
        fromFiId.setFinInstnId(fromFinInstnId);

        Party9Choice from = headFactory.createParty9Choice();
        from.setFIId(fromFiId);

        //TO
        FinancialInstitutionIdentification8 toFinInstnId = headFactory.createFinancialInstitutionIdentification8();
        toFinInstnId.setBICFI("ECSDEE20");

        iso.std.iso._20022.tech.xsd.head_001_001.BranchAndFinancialInstitutionIdentification5 toFiId = headFactory.createBranchAndFinancialInstitutionIdentification5();
        toFiId.setFinInstnId(toFinInstnId);

        Party9Choice to = headFactory.createParty9Choice();
        to.setFIId(toFiId);

        // App header
        BusinessApplicationHeaderV01 businessAppHeader = headFactory.createBusinessApplicationHeaderV01();
        businessAppHeader.setFr(from);
        businessAppHeader.setTo(to);
        businessAppHeader.setBizMsgIdr(id);
        businessAppHeader.setMsgDefIdr("epis");
        businessAppHeader.setCreDt(now());

        mhub.xsd.envelope._01.ObjectFactory exFactory = new mhub.xsd.envelope._01.ObjectFactory();

        Ex.BizMsg bizMsg = exFactory.createExBizMsg();
        bizMsg.setAppHdr(businessAppHeader);

        Envelope envelope = envelopeFactory.createEnvelope();
        envelope.setHeader(header);

        Body body = envelopeFactory.createBody();
        body.getAny().add(input);
        envelope.setBody(body);
        bizMsg.setEnvelope(envelope);
        Ex ex = exFactory.createEx();
        ex.setBizMsg(bizMsg);

        return ex;
    }


    private XMLGregorianCalendar now() {
        try {
            XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            xmlGregorianCalendar.setTimezone(0);
            return xmlGregorianCalendar;
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
