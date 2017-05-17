package ee.tuleva.onboarding.epis.response.application.list;

import ee.tuleva.epis.gen.ApplicationStatusType;
import ee.tuleva.epis.gen.ApplicationType;
import ee.tuleva.epis.gen.ApplicationTypeType;
import ee.tuleva.onboarding.mandate.application.MandateApplicationResponse;
import ee.tuleva.onboarding.mandate.application.MandateApplicationStatus;
import ee.tuleva.onboarding.mandate.application.MandateApplicationType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class EpisApplicationListToMandateApplicationResponseListConverter
        implements Converter<List<ApplicationType>, List<MandateApplicationResponse>>{

    @Override
    public List<MandateApplicationResponse> convert(List<ApplicationType> source) {

        return source.stream()
                .filter(applicationType -> resolveMandateApplicationType(applicationType).isPresent())
                .map(application -> resolveMandateApplicationResponse(application)).collect(Collectors.toList());

    }

    private MandateApplicationResponse resolveMandateApplicationResponse(ApplicationType application) {
        ApplicationType.ApplicationData data = application.getApplicationData();

        MandateApplicationType mandateApplicationType =
                resolveMandateApplicationType(application).orElseThrow(RuntimeException::new);

        return MandateApplicationResponse.builder()
                .type(mandateApplicationType)
                .amount(data.getPaymentAmount())
                .currency(data.getCurrency())
                .date(
                        data.getDocumentDate().toGregorianCalendar().getTime().toInstant()
                )
                .documentNumber(data.getDocumentNumber())
                .id(data.getDocumentId())
                .status(resolveMandateApplicationStatus(data.getStatus()))

                .build();
    }

    private Optional<MandateApplicationType> resolveMandateApplicationType(ApplicationType applicationType) {
        ApplicationTypeType applicationTypeType =
                applicationType.getApplicationData().getApplicationType();

        if(applicationTypeType == ApplicationTypeType.PEVA) {
            return Optional.of(MandateApplicationType.TRANSFER);
        } else if (applicationTypeType == ApplicationTypeType.VAAV) {
            return Optional.of(MandateApplicationType.SELECTION);
        } else {
            return Optional.empty();
        }
    }

    private MandateApplicationStatus resolveMandateApplicationStatus(ApplicationStatusType applicationStatusType) {
        switch(applicationStatusType) {
            case A:
            case R:
            case I:
                return MandateApplicationStatus.COMPLETE;
            case E:
            case T:
            case V:
                return MandateApplicationStatus.FAILED;
            default:
                return MandateApplicationStatus.PENDING;

        }
    }

}
