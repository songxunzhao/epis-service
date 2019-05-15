package ee.tuleva.epis.mandate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MandateResponse {
    private boolean successful;
    private Integer errorCode;
}
