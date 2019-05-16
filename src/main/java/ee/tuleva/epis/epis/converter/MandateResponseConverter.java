package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.mandate.MandateResponse;
import ee.tuleva.epis.mandate.application.MandateApplicationType;
import ee.x_road.epis.producer.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import static ee.tuleva.epis.mandate.application.MandateApplicationType.SELECTION;
import static ee.tuleva.epis.mandate.application.MandateApplicationType.TRANSFER;

@Component
@Slf4j
public class MandateResponseConverter implements Converter<ApplicationWithAddressResponseType, MandateResponse> {

    @NonNull
    public MandateResponse convert(EpisX6ResponseType source, String processId) {
        MandateResponse response = convert(source);
        return convert(response, processId, TRANSFER);
    }

    @NonNull
    public MandateResponse convert(EpisX5ResponseType source, String processId) {
        MandateResponse response = convert(source);
        return convert(response, processId, SELECTION);
    }

    private MandateResponse convert(MandateResponse response, String processId, MandateApplicationType selection) {
        response.setProcessId(processId);
        response.setApplicationType(selection);
        return response;
    }

    @Override
    @NonNull
    public MandateResponse convert(ApplicationWithAddressResponseType source) {
        log.info("Converting to MandateResponse");

        ResultType result = source.getResults();

        return MandateResponse.builder()
            .successful(result.getResult().equals(AnswerType.OK))
            .errorCode(result.getResultCode())
            .errorMessage(result.getErrorTextEng())
            .build();
    }
}
