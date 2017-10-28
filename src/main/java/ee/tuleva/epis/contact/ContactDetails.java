package ee.tuleva.epis.contact;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ContactDetails {

  public enum ContactPreferenceType {E, P} // E - email, P - postal

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

  //FIXME: extract
  private String activeSecondPillarFundIsin;

}
