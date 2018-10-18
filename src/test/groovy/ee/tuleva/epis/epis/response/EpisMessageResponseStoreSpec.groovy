package ee.tuleva.epis.epis.response

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import spock.lang.Specification

class EpisMessageResponseStoreSpec extends Specification {

    AmqpTemplate amqpTemplate = Mock(AmqpTemplate)
    AmqpAdmin amqpAdmin = Mock(AmqpAdmin)
    ObjectMapper objectMapper = new ObjectMapper()

    EpisMessageResponseStore episMessageResponseStore =
            new EpisMessageResponseStore(amqpTemplate, amqpAdmin, objectMapper)

    def "StoreOne: Stores one message per id to read it later"() {
        given:
        String sampleId = "sampleId"
        Object sampleContent = new Object()

        when:
        episMessageResponseStore.storeOne(sampleId, sampleContent)

        then:
        1 * amqpTemplate.convertAndSend(_ as String, sampleContent.toString());

    }

    def "pop: Pops a type casted message for id"() {
        given:
        String sampleId = "sampleId"
        List<String> sampleContent = ["sample content"]
        amqpTemplate.receiveAndConvert(_ as String, 75000) >> JsonOutput.toJson(sampleContent)

        when:
        List<String> result = episMessageResponseStore.pop(sampleId, List.class)

        then:
        result == sampleContent
        1 * amqpAdmin.deleteQueue(_ as String);

    }

}
