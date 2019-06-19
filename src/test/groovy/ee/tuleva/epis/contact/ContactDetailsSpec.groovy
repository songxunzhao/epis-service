package ee.tuleva.epis.contact

import spock.lang.Specification

class ContactDetailsSpec extends Specification {

    def "replaces missing address"() {
        given:
        def contactDetails = ContactDetails.builder()
            .addressRow1(address)
            .country(country)
            .districtCode(district)
            .postalIndex(index)
            .build()

        when:
        def cleanDetails = contactDetails.cleanAddress()

        then:
        cleanDetails.addressRow1 == expectedAddress
        cleanDetails.country == expectedCountry
        cleanDetails.districtCode == expectedDistrict
        cleanDetails.postalIndex == expectedIndex

        where:
        address | country | district | index   | expectedAddress | expectedCountry | expectedDistrict | expectedIndex
        null    | null    | null     | null    | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        null    | "UK"    | null     | null    | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        null    | null    | "0000"   | null    | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        null    | null    | null     | "11111" | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        null    | null    | "0000"   | "11111" | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        null    | "UK"    | "0000"   | "11111" | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        "abc"   | "EE"    | null     | "11111" | "Telliskivi 60" | "EE"            | "0784"           | "10412"
        "abc"   | "UK"    | null     | "11111" | "abc"           | "UK"            | null             | "11111"
        "abc"   | "EE"    | "0000"   | "11111" | "abc"           | "EE"            | "0000"           | "11111"
    }
}
