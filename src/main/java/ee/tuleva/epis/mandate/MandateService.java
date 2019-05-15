package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.ObjectFactoryConfiguration;
import ee.tuleva.epis.contact.ContactDetails;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.LocalDateToXmlGregorianCalendarConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.mandate.application.FundTransferExchange;
import ee.x_road.epis.producer.*;
import ee.x_road.epis.producer.ApplicationRequestType.PersonalData;
import ee.x_road.epis.producer.EpisX6RequestType.ApplicationData.ApplicationRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ObjectFactoryConfiguration.EpisMessageFactory episMessageFactory;
    private final LocalDateToXmlGregorianCalendarConverter dateConverter;
    private final ContactDetailsService contactDetailsService;

    public MandateResponse sendMandate(String personalCode, MandateCommand mandateCommand) {
        ContactDetails contactDetails = contactDetailsService.get(personalCode);
        // sendFutureContributionsApplication(contactDetails, );
        // sendFundTransferApplication(contactDetails, );

        return null;
    }

    public EpisX5Type sendFundTransferApplication(
        ContactDetails contactDetails, List<FundTransferExchange> fundTransferExchanges, Integer pillar,
        LocalDate documentDate, String documentNumber) {

        EpisMessage message = sendFundTransferApplicationQuery(
            contactDetails, fundTransferExchanges, pillar, documentDate, documentNumber);
        EpisX5Type response = episMessageResponseStore.pop(message.getId(), EpisX5Type.class);

        return response;
    }

    private EpisMessage sendFundTransferApplicationQuery(
        ContactDetails contactDetails, List<FundTransferExchange> fundTransferExchanges, Integer pillar,
        LocalDate documentDate, String documentNumber
    ) {
        PersonalData personalData = toPersonalData(contactDetails);
        AddressType address = toAddress(contactDetails);

        EpisX6RequestType.ApplicationData applicationData = episMessageFactory.createEpisX6RequestTypeApplicationData();

        fundTransferExchanges.stream()
            .collect(groupingBy(FundTransferExchange::getSourceFundIsin))
            .forEach((isin, exchanges) -> {
                exchanges.forEach(exchange -> {
                    ApplicationRow row = new ApplicationRow();
                    row.setDestinationISIN(exchange.getTargetFundIsin());
                    row.setPercentage(exchange.getAmount().multiply(new BigDecimal(100)).toBigInteger());
                    applicationData.getApplicationRow().add(row);
                });
                applicationData.setSourceISIN(isin);
                applicationData.setPillar(pillar.toString());
            });

        EpisX6RequestType request = episMessageFactory.createEpisX6RequestType();
        request.setDocumentDate(dateConverter.convert(documentDate));
        request.setDocumentNumber(documentNumber);
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setApplicationData(applicationData);

        EpisX6Type episX6Type = episMessageFactory.createEpisX6Type();
        episX6Type.setRequest(request);

        JAXBElement<EpisX6Type> fundTransferApplication = episMessageFactory.createOSAKUTEVAHETAMISEAVALDUS(episX6Type);
        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, fundTransferApplication);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

    public EpisX5Type sendFutureContributionsApplication(
        ContactDetails contactDetails, String futureContributionFundIsin, Integer pillar,
        LocalDate documentDate, String documentNumber
    ) {
        EpisMessage message = sendFutureContributionsQuery(
            contactDetails, futureContributionFundIsin, pillar, documentDate, documentNumber);
        EpisX5Type response = episMessageResponseStore.pop(message.getId(), EpisX5Type.class);
        return response;
    }

    private EpisMessage sendFutureContributionsQuery(
        ContactDetails contactDetails, String futureContributionFundIsin, Integer pillar,
        LocalDate documentDate, String documentNumber
    ) {
        PersonalData personalData = toPersonalData(contactDetails);
        AddressType address = toAddress(contactDetails);

        EpisX5RequestType.ApplicationData applicationData = episMessageFactory.createEpisX5RequestTypeApplicationData();
        applicationData.setISIN(futureContributionFundIsin);
        applicationData.setPillar(pillar.toString());

        EpisX5RequestType request = episMessageFactory.createEpisX5RequestType();
        request.setDocumentDate(dateConverter.convert(documentDate));
        request.setDocumentNumber(documentNumber);
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setApplicationData(applicationData);

        EpisX5Type episX5Type = episMessageFactory.createEpisX5Type();
        episX5Type.setRequest(request);

        JAXBElement<EpisX5Type> fundContributionApplication = episMessageFactory.createVALIKUAVALDUS(episX5Type);
        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, fundContributionApplication);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

    private PersonalData toPersonalData(ContactDetails contactDetails) {
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

    private AddressType toAddress(ContactDetails contactDetails) {
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
