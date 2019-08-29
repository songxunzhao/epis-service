package ee.tuleva.epis.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
class CompositeTokenServices implements ResourceServerTokenServices {

    private final List<ResourceServerTokenServices> tokenServices;

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken)
        throws AuthenticationException, InvalidTokenException {
        for (ResourceServerTokenServices tokenService : tokenServices) {
            try {
                return tokenService.loadAuthentication(accessToken);
            } catch (InvalidTokenException e) {
                log.info(e.getMessage());
            }
        }
        throw new InvalidTokenException("Could not find any token service that would give a valid access token");
    }

    @Override
    public OAuth2AccessToken readAccessToken(String accessToken) {
        throw new UnsupportedOperationException("Not supported: read access token");

    }
}
