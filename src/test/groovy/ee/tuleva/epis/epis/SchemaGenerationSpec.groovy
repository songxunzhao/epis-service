package ee.tuleva.epis.epis

import ee.x_road.epis.producer.EpisX13RequestType
import ee.x_road.epis.producer.EpisX13Type
import ee.x_road.epis.producer.PersonDataRequestType
import iso.std.iso._20022.tech.xsd.head_001_001.BranchAndFinancialInstitutionIdentification5
import iso.std.iso._20022.tech.xsd.head_001_001.BusinessApplicationHeaderV01
import iso.std.iso._20022.tech.xsd.head_001_001.FinancialInstitutionIdentification8
import iso.std.iso._20022.tech.xsd.head_001_001.Party9Choice
import mhub.xsd.envelope._01.Ex
import org.xmlsoap.schemas.soap.envelope.Body
import org.xmlsoap.schemas.soap.envelope.Envelope
import org.xmlsoap.schemas.soap.envelope.Header
import org.xmlsoap.schemas.soap.envelope.ObjectFactory
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.Marshaller
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.namespace.QName

class SchemaGenerationSpec extends Specification {

    def "can use Object Factories to build DTOs"() {
        expect:
        ObjectFactory envelopeFactory = new ObjectFactory()
        def xRoadFactory = new ee.x_road.xsd.x_road.ObjectFactory()
        def episFactory = new ee.x_road.epis.producer.ObjectFactory()

        Envelope envelope = envelopeFactory.createEnvelope()

        Header header = envelopeFactory.createHeader()
        JAXBElement<String> consumer = xRoadFactory.createConsumer("XMLTULEVA")
        JAXBElement<String> id = xRoadFactory.createId(UUID.randomUUID().toString().replace("-", ""))
        header.getAny().add(consumer)
        header.getAny().add(id)

        Body body = envelopeFactory.createBody()
        EpisX13Type episX13Type = episFactory.createEpisX13Type()
        EpisX13RequestType request = episFactory.createEpisX13RequestType()
        PersonDataRequestType personalData = episFactory.createPersonDataRequestType()
        personalData.setPersonId("44806234555")
        request.getPersonalData().add(personalData)
        episX13Type.setRequest(request)
        JAXBElement<EpisX13Type> saldoTeatis = episFactory.createSALDOTEATIS(episX13Type)
        body.getAny().add(saldoTeatis)

        envelope.setHeader(header)
        envelope.setBody(body)

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

        BranchAndFinancialInstitutionIdentification5 toFiId = headFactory.createBranchAndFinancialInstitutionIdentification5();
        toFiId.setFinInstnId(toFinInstnId);

        Party9Choice to = headFactory.createParty9Choice();
        to.setFIId(toFiId);

        // App header
        BusinessApplicationHeaderV01 businessAppHeader = headFactory.createBusinessApplicationHeaderV01();
        businessAppHeader.setFr(from);
        businessAppHeader.setTo(to);
        businessAppHeader.setMsgDefIdr("epis");
        businessAppHeader.setCreDt(now());

 JAXBElement<BusinessApplicationHeaderV01> appHdr = headFactory.createAppHdr(businessAppHeader);

        mhub.xsd.envelope._01.ObjectFactory exFactory = new mhub.xsd.envelope._01.ObjectFactory();

        Ex.BizMsg bizMsg = exFactory.createExBizMsg();
        bizMsg.setAppHdr(businessAppHeader);
        bizMsg.setEnvelope(envelope);

        Ex ex = exFactory.createEx();
        ex.setBizMsg(bizMsg);


        println toString(ex)

        true

    }

    private XMLGregorianCalendar now() {
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        xmlGregorianCalendar.setTimezone(0);
        return xmlGregorianCalendar;
    }


    private String toString(Ex ex) {
        JAXBContext context = JAXBContext.newInstance(Ex.class, BusinessApplicationHeaderV01.class, Envelope.class, EpisX13Type.class)
        Marshaller marshaller = context.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
        StringWriter stringWriter = new StringWriter()
        JAXBElement jx = new JAXBElement(new QName("Ex"), Ex.class, ex)
        marshaller.marshal(jx, stringWriter)
        return stringWriter.toString()
    }


}
