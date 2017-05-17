package ee.tuleva.onboarding.epis.response

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import spock.lang.Specification

class EpisMessageResponseStoreSpec extends Specification {

    AmqpTemplate amqpTemplate = Mock(AmqpTemplate)
    AmqpAdmin amqpAdmin = Mock(AmqpAdmin)
    EpisMessageResponseStore episMessageResponseStore =
            new EpisMessageResponseStore(amqpTemplate, amqpAdmin)

    def "StoreOne: Stores one message per id to read it later"() {
        given:
        String sampleId = "sampleId"
        Object sampleContent = new Object()

        when:
        episMessageResponseStore.storeOne(sampleId, sampleContent)

        then:
        1 * amqpTemplate.convertAndSend(_ as String, sampleContent.toString());

    }

    def "pop: Pops the message for id"() {
        given:
        String sampleId = "sampleId"
        Object sampleContent = new Object()
        amqpTemplate.receiveAndConvert(_ as String, 10000) >> sampleContent

        when:
        Object result = episMessageResponseStore.pop(sampleId)

        then:
        result == sampleContent
        1 * amqpAdmin.deleteQueue(_ as String);

    }
}
