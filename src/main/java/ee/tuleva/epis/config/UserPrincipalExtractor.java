package ee.tuleva.epis.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserPrincipalExtractor implements PrincipalExtractor {

    @Override
    public Object extractPrincipal(Map<String, Object> map) {
        return map.get("personalCode");
    }

}
