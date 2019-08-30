package ee.tuleva.epis.config

import org.springframework.security.oauth2.common.exceptions.InvalidTokenException
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import spock.lang.Specification

class CompositeTokenServicesSpec extends Specification {

    def tokenService1 = Mock(ResourceServerTokenServices)
    def tokenService2 = Mock(ResourceServerTokenServices)
    def compositeTokenServices = new CompositeTokenServices([tokenService1, tokenService2])
    def accessToken = "accessToken"
    def authentication = Mock(OAuth2Authentication)

    def "takes the first successful token service from the list"() {
        given:
        tokenService1.loadAuthentication(accessToken) >> authentication

        when:
        def returnedAuthentication = compositeTokenServices.loadAuthentication(accessToken)

        then:
        returnedAuthentication == authentication
    }

    def "takes the next token service from the list if one is unsuccessful"() {
        given:
        tokenService1.loadAuthentication(accessToken) >> { throw new InvalidTokenException("oops") }
        tokenService2.loadAuthentication(accessToken) >> authentication

        when:
        def returnedAuthentication = compositeTokenServices.loadAuthentication(accessToken)

        then:
        returnedAuthentication == authentication
    }
}
