package ee.tuleva.epis.contact;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.config.UserPrincipal;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.ContactDetailsToAddressTypeConverter;
import ee.tuleva.epis.epis.converter.ContactDetailsToPersonalDataConverter;
import ee.tuleva.epis.epis.converter.InstantToXmlGregorianCalendarConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.epis.validator.EpisResultValidator;
import ee.x_road.epis.producer.*;
import ee.x_road.epis.producer.ApplicationRequestType.PersonalData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactDetailsService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ContactDetailsConverter contactDetailsConverter;
    private final EpisMessageFactory episMessageFactory;
    private final ContactDetailsToPersonalDataConverter toPersonalDataConverter;
    private final ContactDetailsToAddressTypeConverter toAddressTypeConverter;
    private final InstantToXmlGregorianCalendarConverter timeConverter;
    private final EpisResultValidator resultValidator;

    public ContactDetails getContactDetails(UserPrincipal principal) {
        EpisMessage message = sendGetQuery(principal.getPersonalCode());
        EpisX12Type response = episMessageResponseStore.pop(message.getId(), EpisX12Type.class);
        return contactDetailsConverter.convert(response, principal);
    }

    public void updateContactDetails(String personalCode, ContactDetails contactDetails) {
        validate(personalCode, contactDetails);
        EpisMessage message = sendUpdateQuery(contactDetails);
        EpisX4Type response = episMessageResponseStore.pop(message.getId(), EpisX4Type.class);
        resultValidator.validate(response.getResponse().getResults());
    }

    private EpisMessage sendGetQuery(String personalCode) {
        PersonDataRequestType personalData = episMessageFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX12RequestType request = episMessageFactory.createEpisX12RequestType();
        request.setPersonalData(personalData);

        EpisX12Type episX12Type = episMessageFactory.createEpisX12Type();
        episX12Type.setRequest(request);

        JAXBElement<EpisX12Type> personalDataRequest = episMessageFactory.createISIKUANDMED(episX12Type);
        String id = UUID.randomUUID()
            .toString()
            .replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage);
        return episMessage;
    }

    private void validate(String personalCode, ContactDetails contactDetails) {
        if (!Objects.equals(contactDetails.getPersonalCode(), personalCode)) {
            log.info("Contact details personal code does not match: {}, {}",
                contactDetails.getPersonalCode(), personalCode);
            throw new InvalidPersonalCodeException("Personal codes don't match");
        }
    }

    private EpisMessage sendUpdateQuery(ContactDetails contactDetails) {
        PersonalData personalData = toPersonalDataConverter.convert(contactDetails);
        AddressType address = toAddressTypeConverter.convert(contactDetails);

        EpisX4RequestType request = episMessageFactory.createEpisX4RequestType();
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setDocumentDate(timeConverter.convert(Instant.now()));

        EpisX4Type episX4Type = episMessageFactory.createEpisX4Type();
        episX4Type.setRequest(request);

        JAXBElement<EpisX4Type> personalDataRequest = episMessageFactory.createISIKUANDMETEMUUTMINE(episX4Type);
        String id = UUID.randomUUID()
            .toString()
            .replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage);
        return episMessage;
    }
}
