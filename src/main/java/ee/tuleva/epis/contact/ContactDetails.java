package ee.tuleva.epis.contact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import static ee.tuleva.epis.contact.ContactDetails.LanguagePreferenceType.EST;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDetails {

    private String firstName;

    private String lastName;

    private String personalCode;

    @Builder.Default
    private ContactPreferenceType contactPreference = ContactPreferenceType.P;

    public enum ContactPreferenceType {E, P} // E - email, P - postal

    private String districtCode; // Haldusüksuse kood (EHAK kood). Maakond või linn

    private String addressRow1;

    private String addressRow2;

    private String addressRow3;

    private String postalIndex;

    @Builder.Default
    private String country = "EE";

    @Builder.Default
    private LanguagePreferenceType languagePreference = EST;

    public enum LanguagePreferenceType {EST, RUS, ENG}

    @Builder.Default
    private String noticeNeeded = "Y"; // boolean { 'Y', 'N' }

    private String email;

    private String phoneNumber;

    private String pensionAccountNumber;

    private List<Distribution> thirdPillarDistribution;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Distribution {
        private String activeThirdPillarFundIsin;
        private BigDecimal percentage;
    }

    private String activeSecondPillarFundIsin;

    private boolean isSecondPillarActive;

    private boolean isThirdPillarActive;

    public ContactDetails cleanAddress() {
        if (!hasAddress()) {
            addressRow1 = "Telliskivi 60";
            country = "EE";
            postalIndex = "10412";
            districtCode = "0784";
        }
        return this;
    }

    private boolean hasAddress() {
        return addressRow1 != null && postalIndex != null &&
            (("EE".equals(country) && districtCode != null) || (country != null && !"EE".equals(country)));
    }

}
