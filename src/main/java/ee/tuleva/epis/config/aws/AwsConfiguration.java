package ee.tuleva.epis.config.aws;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfiguration {

    @Bean
    AWSSimpleSystemsManagement awsSimpleSystemsManagement() {
        return AWSSimpleSystemsManagementClientBuilder.defaultClient();
    }

    @Bean
    AwsPropertySource awsPropertySource() {
        return new AwsPropertySource("aws-properties", awsSimpleSystemsManagement());
    }

}
