package ee.tuleva.onboarding.epis.response;

import ee.tuleva.onboarding.epis.EpisMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EpisMessageResponse {

    private String id;
    private String content;
    private EpisMessageType type;

}
