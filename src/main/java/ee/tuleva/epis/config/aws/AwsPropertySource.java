package ee.tuleva.epis.config.aws;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class AwsPropertySource extends MapPropertySource {

    private static Logger log = LoggerFactory.getLogger(AwsPropertySource.class);

    public AwsPropertySource(String name, AWSSimpleSystemsManagement awsSimpleSystemsManagement) {
        super(name, buildSSMMap(awsSimpleSystemsManagement));
    }

    private static Map<String, Object> buildSSMMap(AWSSimpleSystemsManagement awsSimpleSystemsManagement) {
        val config = new HashMap<String, Object>();
        try {
            val parameters = awsSimpleSystemsManagement.getParameters(new GetParametersRequest()
                    .withWithDecryption(true));
            parameters.getParameters().forEach((parameter -> {
                log.info("Found " + parameter.getName() + " " + parameter.getType());
                val springProperty = convertAwsParameterNameToSpringProperty(parameter.getName());
                config.put(springProperty, parameter.getValue());
            }));
        } catch (Exception e) {
            log.error("Could not load parameters from AWS", e);
        }
        return config;
    }

    static String convertAwsParameterNameToSpringProperty(String name) {
        return name.replace("/", ".").toLowerCase();
    }

}
