package ee.tuleva.epis.person;

import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.x_road.epis.producer.EpisX12RequestType;
import ee.x_road.epis.producer.EpisX12Type;
import ee.x_road.epis.producer.PersonDataRequestType;
import iso.std.iso._20022.tech.xsd.head_001_001.BranchAndFinancialInstitutionIdentification5;
import iso.std.iso._20022.tech.xsd.head_001_001.BusinessApplicationHeaderV01;
import iso.std.iso._20022.tech.xsd.head_001_001.FinancialInstitutionIdentification8;
import iso.std.iso._20022.tech.xsd.head_001_001.Party9Choice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;
import org.xmlsoap.schemas.soap.envelope.Body;
import org.xmlsoap.schemas.soap.envelope.Envelope;
import org.xmlsoap.schemas.soap.envelope.Header;
import org.xmlsoap.schemas.soap.envelope.ObjectFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    //TODO: replace with bean
    private ObjectFactory objectFactory = new ObjectFactory();

    EpisX12Type get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);
        EpisX12Type response = episMessageResponseStore.pop(message.getId(), EpisX12Type.class);
        return response;
    }

    private EpisMessage sendQuery(String personalCode) {
        String id = UUID.randomUUID().toString().replace("-", "");

        ObjectFactory envelopeFactory = objectFactory;
        ee.x_road.xsd.x_road.ObjectFactory xRoadFactory = new ee.x_road.xsd.x_road.ObjectFactory();
        ee.x_road.epis.producer.ObjectFactory episFactory = new ee.x_road.epis.producer.ObjectFactory();

        PersonDataRequestType personalData = episFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX12RequestType request = episFactory.createEpisX12RequestType();
        request.setPersonalData(personalData);

        EpisX12Type episX12Type = episFactory.createEpisX12Type();
        episX12Type.setRequest(request);

        JAXBElement<EpisX12Type> isikuAndmed = episFactory.createISIKUANDMED(episX12Type);
        Body body = envelopeFactory.createBody();
        body.getAny().add(isikuAndmed);

        Header header = envelopeFactory.createHeader();
        JAXBElement<String> consumer = xRoadFactory.createConsumer("XMLTULEVA");
        JAXBElement<String> headerId = xRoadFactory.createId(id);
        header.getAny().add(consumer);
        header.getAny().add(headerId);

        Envelope envelope = envelopeFactory.createEnvelope();
        envelope.setHeader(header);
        envelope.setBody(body);

        JAXBElement<Envelope> wrappedEnvelope = envelopeFactory.createEnvelope(envelope);

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

        JAXBElement<BusinessApplicationHeaderV01> appHdr = headFactory.createAppHdr(businessAppHeader);

        mhub.xsd.envelope._01.ObjectFactory exFactory = new mhub.xsd.envelope._01.ObjectFactory();

        Ex.BizMsg bizMsg = exFactory.createExBizMsg();
        bizMsg.setAppHdr(businessAppHeader);
        bizMsg.setEnvelope(envelope);

        Ex ex = exFactory.createEx();
        ex.setBizMsg(bizMsg);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());

        return episMessage;
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
