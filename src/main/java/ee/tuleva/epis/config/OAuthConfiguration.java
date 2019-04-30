package ee.tuleva.epis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static ee.tuleva.epis.config.OAuthConfiguration.ResourceServerPathConfiguration.RESOURCE_REQUEST_MATCHER_BEAN;

@Configuration
public class OAuthConfiguration {

    @Configuration
    public static class ResourceServerPathConfiguration {
        public static final String RESOURCE_REQUEST_MATCHER_BEAN = "resourceServerRequestMatcher";

        @Bean(RESOURCE_REQUEST_MATCHER_BEAN)
        public RequestMatcher resources() {
            return new AntPathRequestMatcher("/v1/**");
        }
    }

    @Configuration
    @EnableResourceServer
    @RequiredArgsConstructor
    protected static class OAuthResourceServerConfig extends ResourceServerConfigurerAdapter {

        @Qualifier(RESOURCE_REQUEST_MATCHER_BEAN)
        final RequestMatcher resources;

        private static final String RESOURCE_ID = "onboarding-service";

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(RESOURCE_ID);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .requestMatcher(resources).authorizeRequests()
                .anyRequest().authenticated()
                .and().csrf().ignoringRequestMatchers(resources);
        }
    }
}
