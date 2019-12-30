package ee.tuleva.epis.epis.converter;

import ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory;
import ee.tuleva.epis.contact.ContactDetails;
import ee.x_road.epis.producer.AddressType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@AllArgsConstructor
public class ContactDetailsToAddressTypeConverter implements Converter<ContactDetails, AddressType> {

    private final EpisMessageFactory episMessageFactory;

    @Override
    @NonNull
    public AddressType convert(ContactDetails contactDetails) {
        AddressType address = episMessageFactory.createAddressType();
        address.setAddressRow1(contactDetails.getAddressRow1());
        address.setAddressRow2(contactDetails.getAddressRow2());
        address.setAddressRow3(!isEstonia(contactDetails) ? contactDetails.getAddressRow3() : null);
        address.setCountry(contactDetails.getCountry());
        address.setPostalIndex(contactDetails.getPostalIndex());
        address.setTerritory(!isEstonia(contactDetails) ? contactDetails.getDistrictCode() : null);
        return address;
    }

    private boolean isEstonia(ContactDetails contactDetails) {
        return "EE".equals(contactDetails.getCountry());
    }
}
