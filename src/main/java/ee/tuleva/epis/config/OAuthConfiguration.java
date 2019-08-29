package ee.tuleva.epis.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

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

        private static final String RESOURCE_ID = "epis-service";

        private final UserInfoTokenServices userInfoTokenServices;
        private final ResourceServerProperties properties;

        @Override
        public void configure(ResourceServerSecurityConfigurer config) {
            config.resourceId(RESOURCE_ID);
            config.tokenServices(compositeTokenServices());
        }

        private ResourceServerTokenServices compositeTokenServices() {
            return new CompositeTokenServices(Arrays.asList(userInfoTokenServices, remoteTokenServices()));
        }

        private RemoteTokenServices remoteTokenServices() {
            RemoteTokenServices services = new RemoteTokenServices();
            services.setCheckTokenEndpointUrl(properties.getTokenInfoUri());
            services.setClientId(properties.getClientId());
            services.setClientSecret(properties.getClientSecret());
            return services;
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
