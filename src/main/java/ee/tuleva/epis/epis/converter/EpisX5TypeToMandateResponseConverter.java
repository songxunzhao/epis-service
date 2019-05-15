package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.mandate.MandateResponse;
import ee.x_road.epis.producer.AnswerType;
import ee.x_road.epis.producer.EpisX5Type;
import ee.x_road.epis.producer.ResultType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EpisX5TypeToMandateResponseConverter implements Converter<EpisX5Type, MandateResponse> {

    @Override
    public MandateResponse convert(EpisX5Type source) {
        log.info("Converting EpisX5Type to MandateResponse");

        ResultType result = source.getResponse().getResults();

        return MandateResponse.builder()
            .successful(result.getResult().equals(AnswerType.OK))
            .errorCode(result.getResultCode())
            .errorMessage(result.getErrorTextEng())
            .build();
    }
}
