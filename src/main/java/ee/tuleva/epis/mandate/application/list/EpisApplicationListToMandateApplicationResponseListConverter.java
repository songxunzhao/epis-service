package ee.tuleva.epis.mandate.application.list;

import ee.tuleva.epis.mandate.application.MandateApplicationStatus;
import ee.tuleva.epis.mandate.application.MandateApplicationType;
import ee.tuleva.epis.mandate.application.MandateExchangeApplicationResponse;
import ee.x_road.epis.producer.*;
import ee.x_road.epis.producer.ApplicationType.ApplicationData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ee.tuleva.epis.mandate.application.MandateApplicationStatus.*;
import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION;
import static ee.tuleva.epis.mandate.application.MandateApplicationType.TRANSFER;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class EpisApplicationListToMandateApplicationResponseListConverter
    implements Converter<List<ApplicationType>, List<MandateExchangeApplicationResponse>> {

    @Override
    public List<MandateExchangeApplicationResponse> convert(List<ApplicationType> source) {
        return source.stream()
            .filter(this::isExchangeApplication)
            .flatMap(application -> resolveMandateExchangeApplicationResponse(application).stream())
            .collect(toList());
    }

    private List<MandateExchangeApplicationResponse> resolveMandateExchangeApplicationResponse(
        ApplicationType application) {
        if (application instanceof ExchangeApplicationType) {
            return resolveMandateExchangeApplicationResponse((ExchangeApplicationType) application);
        } else if (application instanceof SwitchApplicationType) {
            return resolveMandateExchangeApplicationResponse((SwitchApplicationType) application);
        }
        throw new IllegalStateException("Unknown ApplicationType");
    }

    private List<MandateExchangeApplicationResponse> resolveMandateExchangeApplicationResponse(ExchangeApplicationType application) {
        ApplicationData data = application.getApplicationData();

        if (application.getExchangeApplicationRows() == null ||
            application.getExchangeApplicationRows().getExchangeApplicationRow() == null) {
            return emptyList();
        }

        return application.getExchangeApplicationRows().getExchangeApplicationRow().stream()
            .map(exchangeApplicationRow -> MandateExchangeApplicationResponse.builder()
                .sourceFundIsin(application.getSourceISIN())
                .targetFundIsin(exchangeApplicationRow.getDestinationISIN())
                .amount(exchangeApplicationRow.getPercentage().scaleByPowerOfTen(-2))
                .currency(data.getCurrency())
                .date(data.getDocumentDate().toGregorianCalendar().getTime().toInstant())
                .documentNumber(data.getDocumentNumber())
                .id(data.getDocumentId())
                .status(resolveMandateApplicationStatus(data.getStatus()))
                .build())
            .collect(toList());
    }

    private List<MandateExchangeApplicationResponse> resolveMandateExchangeApplicationResponse(SwitchApplicationType application) {
        ApplicationData data = application.getApplicationData();

        if (application.getApplicationRows() == null ||
            application.getApplicationRows().getApplicationRow() == null) {
            return emptyList();
        }

        return application.getApplicationRows().getApplicationRow().stream()
            .map(applicationRow -> MandateExchangeApplicationResponse.builder()
                .sourceFundIsin(application.getSourceISIN())
                .targetFundIsin(applicationRow.getISIN())
                .amount(applicationRow.getUnitAmount())
                .currency(data.getCurrency())
                .date(data.getDocumentDate().toGregorianCalendar().getTime().toInstant())
                .documentNumber(data.getDocumentNumber())
                .id(data.getDocumentId())
                .status(resolveMandateApplicationStatus(data.getStatus()))
                .build())
            .collect(toList());
    }

    private boolean isExchangeApplication(ApplicationType applicationType) {
        return resolveMandateApplicationType(applicationType)
            .equals(Optional.of(TRANSFER));
    }

    private Optional<MandateApplicationType> resolveMandateApplicationType(ApplicationType applicationType) {
        ApplicationTypeType type = applicationType.getApplicationData().getApplicationType();

        switch (type) {
            case PEVA: // 2nd pillar
            case SWI: // 3rd pillar
                return Optional.of(TRANSFER);
            case VAAV: // 2nd pillar
            case SUB: // 3rd pillar
                return Optional.of(SELECTION);
            default:
                return Optional.empty();
        }
    }

    private MandateApplicationStatus resolveMandateApplicationStatus(ApplicationStatusType type) {
        switch (type) {
            case R:
                return COMPLETE;
            case V:
            case T:
            case E:
                return FAILED;
            default:
                return PENDING;
        }
    }

}
