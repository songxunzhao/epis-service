package ee.tuleva.epis.epis.converter


import spock.lang.Specification

import static ee.tuleva.epis.config.ObjectFactoryConfiguration.EpisMessageFactory
import static ee.tuleva.epis.contact.ContactDetailsFixture.contactDetailsFixture

class ContactDetailsToAddressTypeConverterSpec extends Specification {

    def episMessageFactory = new EpisMessageFactory()

    def converter = new ContactDetailsToAddressTypeConverter(episMessageFactory)

    def "converts contact details to address and ignores addressRow3 for Estonian addresses"() {
        given:
        def contactDetails = contactDetailsFixture()

        when:
        def address = converter.convert(contactDetails)

        then:
        address.addressRow1 == contactDetails.addressRow1
        address.addressRow2 == contactDetails.addressRow2
        address.addressRow3 == null
        address.country == contactDetails.country
        address.postalIndex == contactDetails.postalIndex
        address.territory == contactDetails.districtCode
    }

    def "converts contact details to address and ignores district code for non-Estonian addresses"() {
        given:
        def contactDetails = contactDetailsFixture()
        contactDetails.country = 'US'

        when:
        def address = converter.convert(contactDetails)

        then:
        address.addressRow1 == contactDetails.addressRow1
        address.addressRow2 == contactDetails.addressRow2
        address.addressRow3 == contactDetails.addressRow3
        address.country == contactDetails.country
        address.postalIndex == contactDetails.postalIndex
        address.territory == null
    }
}
