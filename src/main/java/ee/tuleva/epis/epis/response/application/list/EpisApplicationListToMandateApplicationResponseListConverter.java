package ee.tuleva.epis.epis.response.application.list;

import ee.tuleva.epis.gen.ApplicationStatusType;
import ee.tuleva.epis.gen.ApplicationType;
import ee.tuleva.epis.gen.ApplicationTypeType;
import ee.tuleva.epis.gen.ExchangeApplicationType;
import ee.tuleva.epis.mandate.application.MandateApplicationStatus;
import ee.tuleva.epis.mandate.application.MandateApplicationType;
import ee.tuleva.epis.mandate.application.MandateExchangeApplicationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EpisApplicationListToMandateApplicationResponseListConverter
        implements Converter<List<ApplicationType>, List<MandateExchangeApplicationResponse>>{

    @Override
    public List<MandateExchangeApplicationResponse> convert(List<ApplicationType> source) {
        return source.stream()
                .filter(application -> isExchangeApplication(application))
                .map(application -> (ExchangeApplicationType) application)
                .flatMap(application -> resolveMandateExchangeApplicationResponse(application).stream())
                .collect(Collectors.toList());
    }

    private List<MandateExchangeApplicationResponse> resolveMandateExchangeApplicationResponse(ExchangeApplicationType application) {
        ApplicationType.ApplicationData data = application.getApplicationData();

        if(application.getExchangeApplicationRows() == null ||
                application.getExchangeApplicationRows().getExchangeApplicationRow() == null) {
            return Arrays.asList();
        }

        return application.getExchangeApplicationRows().getExchangeApplicationRow().stream()
                .map(exchangeApplicationRow -> {
                    return MandateExchangeApplicationResponse.builder()
                            .sourceFundIsin(application.getSourceISIN())
                            .targetFundIsin(exchangeApplicationRow.getDestinationISIN())
                            .amount(exchangeApplicationRow.getPercentage().scaleByPowerOfTen(-2))
                            .currency(data.getCurrency())
                            .date(
                                    data.getDocumentDate().toGregorianCalendar().getTime().toInstant()
                            )
                            .documentNumber(data.getDocumentNumber())
                            .id(data.getDocumentId())
                            .status(resolveMandateApplicationStatus(data.getStatus()))

                            .build();
                }).collect(Collectors.toList());

    }

    private boolean isExchangeApplication(ApplicationType applicationType) {
        return resolveMandateApplicationType(applicationType)
                .equals(Optional.of(MandateApplicationType.TRANSFER));
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
            case R:
                return MandateApplicationStatus.COMPLETE;
            case V:
            case T:
            case E:
                return MandateApplicationStatus.FAILED;
            default:
                return MandateApplicationStatus.PENDING;

        }
    }

}
