package ee.tuleva.epis.config.aws

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersResult
import com.amazonaws.services.simplesystemsmanagement.model.Parameter
import spock.lang.Specification

class AwsPropertySourceSpec extends Specification {
    private AWSSimpleSystemsManagement awsClient
    private AwsPropertySource awsPropertySource

    def setup() {
        awsClient = Mock(AWSSimpleSystemsManagement)
        awsClient.getParameters(_) >> new GetParametersResult()
                .withParameters(new Parameter().withName("test").withValue("testvalue"))
        awsPropertySource = new AwsPropertySource("aws-properties", awsClient)
    }

    def "converts property"() {
        given:
        String ssmParameter = "key"
        when:
        String springParameter = awsPropertySource.convertAwsParameterNameToSpringProperty(ssmParameter)
        then:
        springParameter == 'key'
    }

    def "converts multilevel property"() {
        given:
        String ssmParameter = "key/more/levels"
        when:
        String springParameter = awsPropertySource.convertAwsParameterNameToSpringProperty(ssmParameter)
        then:
        springParameter == 'key.more.levels'
    }

    def "retrieves parameters from AWS"() {
        given:
        String propKey = "test"
        when:
        def prop = awsPropertySource.getProperty(propKey)
        then:
        prop == "testvalue"
    }

}
