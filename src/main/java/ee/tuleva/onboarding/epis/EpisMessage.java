package ee.tuleva.onboarding.epis;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EpisMessage {

    private String id;
    private String message;
    private EpisMessageType type;

}
