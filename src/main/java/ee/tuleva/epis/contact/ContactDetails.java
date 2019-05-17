package ee.tuleva.epis.contact;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class ContactDetails {

    public enum ContactPreferenceType {E, P} // E - email, P - postal

    private String firstName;

    private String lastName;

    private String personalCode;

    private ContactPreferenceType contactPreference;

    private String districtCode;

    private String addressRow1;

    private String addressRow2;

    private String addressRow3;

    private String postalIndex;

    private String country;

    public enum LanguagePreferenceType {EST, RUS, ENG}

    private LanguagePreferenceType languagePreference;

    private String noticeNeeded; // boolean { 'Y', 'N' }

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

    //FIXME: extract
    private String activeSecondPillarFundIsin;

}
