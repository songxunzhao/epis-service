package ee.tuleva.epis.person

import ee.tuleva.epis.BaseControllerSpec
import ee.x_road.epis.producer.EpisX12ResponseType
import ee.x_road.epis.producer.EpisX12Type
import ee.x_road.epis.producer.PersonType
import org.springframework.http.MediaType

import static org.hamcrest.Matchers.is
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class PersonControllerSpec extends BaseControllerSpec  {

    PersonService personService = Mock(PersonService)

    PersonController controller = new PersonController(personService)

    def "Getting a person works"() {
        given:
        EpisX12Type samplePerson = samplePerson()

        def mvc = mockMvc(controller)
        1 * personService.get(_) >> samplePerson

        expect:
        mvc.perform(get("/v1/persons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath('$.response.personalData.firstName',
                is(samplePerson.response.personalData.firstName)))
    }

    EpisX12Type samplePerson() {
        PersonType person = new PersonType();
        person.setFirstName("Jordan")

        EpisX12ResponseType response = new EpisX12ResponseType();
        response.setPersonalData(person);

        EpisX12Type samplePerson = new EpisX12Type();
        samplePerson.setResponse(response)

        return samplePerson;
    }

}
