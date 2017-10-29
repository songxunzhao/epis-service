package ee.tuleva.epis.epis.response

import com.fasterxml.jackson.databind.ObjectMapper
import iso.std.iso._20022.tech.xsd.head_001_001.BusinessApplicationHeaderV01
import mhub.xsd.envelope._01.Ex
import org.springframework.jms.support.converter.MarshallingMessageConverter
import org.springframework.jms.support.converter.MessageConverter
import spock.lang.Specification

import javax.jms.Message
import javax.xml.bind.JAXBElement

class EpisMessageListenerSpec extends Specification {

    EpisMessageResponseStore episMessageResponseStore = Mock(EpisMessageResponseStore)
    MessageConverter messageConverter = Mock(MarshallingMessageConverter)
    ObjectMapper objectMapper = new ObjectMapper()

    EpisMessageListener service = new EpisMessageListener(
            episMessageResponseStore,
            messageConverter,
            objectMapper
    )

    def "On message, store it"() {
        given:

        String sampleId = 'sampleId';

        messageConverter.fromMessage(sampleMessage) >> Mock(Ex, {
            getBizMsg() >> Mock(Ex.BizMsg, {
                getAppHdr() >> Mock(BusinessApplicationHeaderV01, {
                    getBizMsgIdr() >> sampleId
                })
                getAny() >> Mock(JAXBElement,{
                    getValue() >> [:]
                })
            })
        })

        when:
        service.processorListener().onMessage(sampleMessage)
        then:
        1 * episMessageResponseStore.storeOne(
                sampleId, "{}"
        );
    }

    Message sampleMessage = Mock(Message)
}
