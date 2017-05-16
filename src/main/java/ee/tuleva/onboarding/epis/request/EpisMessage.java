package ee.tuleva.onboarding.epis.request;

import ee.tuleva.onboarding.epis.EpisMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EpisMessage {

    private String id;
    private String content;
    private EpisMessageType type;

}
