package ee.tuleva.epis.epis.request;

import ee.tuleva.epis.epis.EpisMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EpisMessage {

    private String id;
    private String content;
    private Object payload;
    private EpisMessageType type;

}
