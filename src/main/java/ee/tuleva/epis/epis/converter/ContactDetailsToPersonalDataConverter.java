package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.config.ObjectFactoryConfiguration;
import ee.tuleva.epis.contact.ContactDetails;
import ee.x_road.epis.producer.ApplicationRequestType.PersonalData;
import ee.x_road.epis.producer.LangType;
import ee.x_road.epis.producer.MailType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ContactDetailsToPersonalDataConverter implements Converter<ContactDetails, PersonalData> {

    private final ObjectFactoryConfiguration.EpisMessageFactory episMessageFactory;

    @Override
    public PersonalData convert(ContactDetails contactDetails) {
        PersonalData personalData = episMessageFactory.createApplicationRequestTypePersonalData();
        personalData.setPersonId(contactDetails.getPersonalCode());
        personalData.setEMAIL(contactDetails.getEmail());
        personalData.setContactPreference(MailType.valueOf(contactDetails.getContactPreference().name()));
        personalData.setFirstName(contactDetails.getFirstName());
        personalData.setName(contactDetails.getLastName());
        personalData.setPhone(contactDetails.getPhoneNumber());
        personalData.setExtractFlag(contactDetails.getNoticeNeeded());
        personalData.setLanguagePreference(LangType.valueOf(contactDetails.getLanguagePreference().name()));
        return personalData;
    }
}
