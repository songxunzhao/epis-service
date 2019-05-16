package ee.tuleva.epis.epis.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class EpisMessage {

    private String id;
    private Object payload;

}
