package ee.tuleva.onboarding.epis.response;

import ee.tuleva.epis.gen.ApplicationType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class EpisApplicationListResponse {

    private String id;
    private List<ApplicationType> applications;

}
