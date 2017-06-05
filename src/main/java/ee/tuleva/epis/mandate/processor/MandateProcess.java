package ee.tuleva.epis.mandate.processor;

import ee.tuleva.epis.mandate.application.MandateApplicationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Optional;

@Entity
@Table(name = "mandate_process")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MandateProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long mandateId;

    private String processId;

    @Enumerated(EnumType.STRING)
    private MandateApplicationType type;

    private Boolean successful;

    private Integer errorCode;

    @NotNull
    private Instant createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = Instant.now();
    }

    public Optional<Boolean> isSuccessful() {
        return Optional.ofNullable(successful);
    }

    public Optional<Boolean> getSuccessful() {
        return Optional.ofNullable(successful);
    }

    public Optional<Integer> getErrorCode() {
        return Optional.ofNullable(errorCode);
    }

}