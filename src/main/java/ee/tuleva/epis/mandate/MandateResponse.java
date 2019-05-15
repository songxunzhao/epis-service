package ee.tuleva.epis.mandate;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandateResponse {
    private boolean successful;
    private Integer errorCode;
    private String errorMessage;
}
