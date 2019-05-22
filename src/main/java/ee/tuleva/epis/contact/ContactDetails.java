package ee.tuleva.epis.contact;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import static ee.tuleva.epis.contact.ContactDetails.ContactPreferenceType.*;
import static ee.tuleva.epis.contact.ContactDetails.LanguagePreferenceType.*;

@Builder
@Data
public class ContactDetails {

    private String firstName;

    private String lastName;

    private String personalCode;

    public enum ContactPreferenceType {E, P} // E - email, P - postal

    @Builder.Default
    private ContactPreferenceType contactPreference = E;

    private String districtCode;

    private String addressRow1;

    private String addressRow2;

    private String addressRow3;

    private String postalIndex;

    @Builder.Default
    private String country = "EE";

    public enum LanguagePreferenceType {EST, RUS, ENG}

    @Builder.Default
    private LanguagePreferenceType languagePreference = EST;

    @Builder.Default
    private String noticeNeeded = "Y"; // boolean { 'Y', 'N' }

    private String email;

    private String phoneNumber;

    private String pensionAccountNumber;

    @Data
    @Builder
    public static class Distribution {
        private String activeThirdPillarFundIsin;
        private BigDecimal percentage;
    }

    private List<Distribution> thirdPillarDistribution;

    private String activeSecondPillarFundIsin;

}
