package ee.tuleva.epis.person

import ee.tuleva.epis.epis.EpisService
import ee.tuleva.epis.epis.response.EpisMessageResponseStore
import ee.x_road.epis.producer.EpisX12RequestType
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.PersonDataRequestType
import mhub.xsd.envelope._01.Ex
import spock.lang.Specification

import javax.xml.bind.JAXBElement

class PersonServiceSpec extends Specification {

    EpisService episService = Mock(EpisService);
    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore);

    PersonService service = new PersonService(episService, episMessageResponseStore)

    def "Get person"() {
        given:
        String personalCode = "38080808080"
        EpisX12Type sampleResponse = new EpisX12Type()

        episMessageResponseStore.pop(_, EpisX12Type.class) >> sampleResponse

        1 * episService.send({ Ex ex ->
            String requestPersonalCode = ((PersonDataRequestType)((EpisX12RequestType)((EpisX12Type)((JAXBElement)ex.getBizMsg().envelope.body.any.get(0)).value).request).personalData).personId
            String headerContent = ((JAXBElement)ex.getBizMsg().envelope.header.any.get(0)).value
            String headerId = ex.bizMsg.appHdr.bizMsgIdr.toString()
            String messageId = ((JAXBElement)ex.bizMsg.envelope.header.any.get(1)).value

            String from = ex.bizMsg.appHdr.fr.fiId.finInstnId.bicfi
            String to = ex.bizMsg.appHdr.to.fiId.finInstnId.bicfi
            String messageDef =  ex.bizMsg.appHdr.msgDefIdr

            return (
                    personalCode == personalCode &&
                            headerContent == "XMLTULEVA" &&
                            headerId == messageId && headerId != null & headerId != "" &&
                            requestPersonalCode == personalCode &&
                            from == "TULEVA20" &&
                            to == "ECSDEE20" &&
                            messageDef == "epis"
            )
        })
        when:
        EpisX12Type response = service.get(personalCode)

        then:
        response == sampleResponse

    }

}
