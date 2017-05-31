package ee.tuleva.epis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import ee.tuleva.epis.error.ErrorHandlingControllerAdvice
import ee.tuleva.epis.error.response.ErrorResponseEntityFactory
import ee.tuleva.epis.error.response.InputErrorsConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import spock.lang.Specification

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

class BaseControllerSpec extends Specification {

    protected final static ObjectMapper mapper = new ObjectMapper()

    protected MockMvc mockMvc(Object... controllers) {
        return getMockMvcWithControllerAdvice(controllers)
                .build()
    }

    private StandaloneMockMvcBuilder getMockMvcWithControllerAdvice(Object... controllers) {
        return standaloneSetup(controllers)
                .setMessageConverters(jacksonMessageConverter())
                .setControllerAdvice(errorHandlingControllerAdvice())
    }

    private ErrorHandlingControllerAdvice errorHandlingControllerAdvice() {
        ErrorHandlingControllerAdvice controllerAdvice =
                new ErrorHandlingControllerAdvice(new ErrorResponseEntityFactory(new InputErrorsConverter()));

        return controllerAdvice;
    }

    private MappingJackson2HttpMessageConverter jacksonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter()
        converter.setObjectMapper(objectMapper)
        return converter
    }

}
