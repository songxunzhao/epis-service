package ee.tuleva.epis.config;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static ee.tuleva.epis.config.OAuthConfiguration.ResourceServerPathConfiguration.RESOURCE_REQUEST_MATCHER_BEAN;

@EnableWebSecurity
@EnableOAuth2Client
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Qualifier(RESOURCE_REQUEST_MATCHER_BEAN)
    final RequestMatcher resources;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        val nonResources = new NegatedRequestMatcher(resources);
        http.requestMatcher(nonResources)
            .authorizeRequests()
            .requestMatchers(EndpointRequest.to("health")).permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint().excluding("health")).authenticated()
            .regexMatchers("/", "/swagger-ui.html")
            .permitAll()
            .anyRequest().authenticated()
            .and()
            .httpBasic();
    }
}
