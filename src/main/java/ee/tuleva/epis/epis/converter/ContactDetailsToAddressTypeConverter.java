package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.contact.ContactDetails;
import ee.x_road.epis.producer.AddressType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ContactDetailsToAddressTypeConverter implements Converter<ContactDetails, AddressType> {

    private final EpisMessageFactory episMessageFactory;

    @Override
    public AddressType convert(ContactDetails contactDetails) {
        AddressType address = episMessageFactory.createAddressType();
        address.setAddressRow1(contactDetails.getAddressRow1());
        address.setAddressRow2(contactDetails.getAddressRow2());
        address.setAddressRow3(contactDetails.getAddressRow3());
        address.setCountry(contactDetails.getCountry());
        address.setPostalIndex(contactDetails.getPostalIndex());
        address.setTerritory(contactDetails.getDistrictCode());
        return address;
    }
}
