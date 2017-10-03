package ee.tuleva.epis.epis

import ee.x_road.epis.producer.EpisX13RequestType
import ee.x_road.epis.producer.EpisX13Type
import ee.x_road.epis.producer.PersonDataRequestType
import org.w3._2003._05.soap_envelope.Body
import org.w3._2003._05.soap_envelope.Envelope
import org.w3._2003._05.soap_envelope.Header
import org.w3._2003._05.soap_envelope.ObjectFactory
import spock.lang.Specification

import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.Marshaller
import javax.xml.namespace.QName

class SchemaGenerationSpec extends Specification {

    def "can use ObjectFactory to build DTOs"() {
        expect:
        ObjectFactory envelopeFactory = new ObjectFactory()
        def xRoadFactory = new ee.x_road.xsd.x_road.ObjectFactory()
        def episFactory = new ee.x_road.epis.producer.ObjectFactory()

        Envelope envelope = envelopeFactory.createEnvelope()

        Header header = envelopeFactory.createHeader()
        JAXBElement<String> consumer = xRoadFactory.createConsumer("XMLTULEVA")
        header.getAny().add(consumer)

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


        toString(envelope) ==
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Envelope xmlns:ns2="http://epis.x-road.ee/producer/" xmlns:ns3="http://www.w3.org/2003/05/soap-envelope">
    <ns3:Header>
        <ns4:consumer xmlns:ns4="http://x-road.ee/xsd/x-road.xsd">XMLTULEVA</ns4:consumer>
    </ns3:Header>
    <ns3:Body>
        <ns2:SALDOTEATIS>
            <ns2:Request>
                <ns2:PersonalData PersonId="44806234555"/>
            </ns2:Request>
        </ns2:SALDOTEATIS>
    </ns3:Body>
</Envelope>
"""

    }

    private String toString(Envelope envelope) {
        JAXBContext context = JAXBContext.newInstance(Envelope.class, EpisX13Type.class)
        Marshaller marshaller = context.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
        StringWriter stringWriter = new StringWriter()
        JAXBElement jx = new JAXBElement(new QName("Envelope"), Envelope.class, envelope)
        marshaller.marshal(jx, stringWriter)
        return stringWriter.toString()
    }


}
