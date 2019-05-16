package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.ObjectFactoryConfiguration;
import ee.tuleva.epis.contact.ContactDetails;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.InstantToXmlGregorianCalendarConverter;
import ee.tuleva.epis.epis.converter.MandateResponseConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class MandateService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ObjectFactoryConfiguration.EpisMessageFactory episMessageFactory;
    private final InstantToXmlGregorianCalendarConverter timeConverter;
    private final ContactDetailsService contactDetailsService;
    private final MandateResponseConverter mandateResponseConverter;

    public List<MandateResponse> sendMandate(String personalCode, MandateCommand mandateCommand) {
        ContactDetails contactDetails = contactDetailsService.get(personalCode);
        List<MandateResponse> mandateResponses = new ArrayList<>();

        if (mandateCommand.getFundTransferExchanges() != null) {
            List<MandateResponse> transferResponses = sendFundTransferApplications(contactDetails, mandateCommand);
            mandateResponses.addAll(transferResponses);
        }

        if (mandateCommand.getFutureContributionFundIsin() != null) {
            MandateResponse selectionResponse = sendSelectionApplication(contactDetails, mandateCommand);
            mandateResponses.add(selectionResponse);
        }

        return mandateResponses;
    }

    List<MandateResponse> sendFundTransferApplications(ContactDetails contactDetails, MandateCommand mandateCommand) {
        List<MandateResponse> mandateResponses = mandateCommand.getFundTransferExchanges().stream()
            .collect(groupingBy(FundTransferExchange::getSourceFundIsin)).values().stream()
            .map(exchanges -> {
                EpisMessage message = sendFundTransferApplicationQuery(
                    contactDetails,
                    exchanges,
                    mandateCommand.getPillar(),
                    mandateCommand.getCreatedDate(),
                    mandateCommand.getId());
                EpisX6Type response = episMessageResponseStore.pop(message.getId(), EpisX6Type.class);
                MandateResponse mandateResponse = mandateResponseConverter.convert(response.getResponse(),
                    message.getId());
                return mandateResponse;
            })
            .collect(toList());

        return mandateResponses;
    }

    private EpisMessage sendFundTransferApplicationQuery(
        ContactDetails contactDetails, List<FundTransferExchange> fundTransferExchanges, Integer pillar,
        Instant createdDate, Long mandateId
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
        request.setDocumentDate(timeConverter.convert(createdDate));
        request.setDocumentNumber(mandateId.toString());
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setApplicationData(applicationData);

        EpisX6Type episX6Type = episMessageFactory.createEpisX6Type();
        episX6Type.setRequest(request);

        JAXBElement<EpisX6Type> fundTransferApplication = episMessageFactory.createOSAKUTEVAHETAMISEAVALDUS(episX6Type);

        String id = fundTransferExchanges.get(0).getProcessId();

        Ex ex = episMessageWrapper.wrap(id, fundTransferApplication);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

    MandateResponse sendSelectionApplication(ContactDetails contactDetails, MandateCommand mandateCommand) {
        EpisMessage message = sendFutureContributionsQuery(contactDetails, mandateCommand);
        EpisX5Type response = episMessageResponseStore.pop(message.getId(), EpisX5Type.class);
        MandateResponse mandateResponse = mandateResponseConverter.convert(response.getResponse(), message.getId());
        return mandateResponse;
    }

    private EpisMessage sendFutureContributionsQuery(ContactDetails contactDetails, MandateCommand mandateCommand) {
        PersonalData personalData = toPersonalData(contactDetails);
        AddressType address = toAddress(contactDetails);

        EpisX5RequestType.ApplicationData applicationData = episMessageFactory.createEpisX5RequestTypeApplicationData();
        applicationData.setISIN(mandateCommand.getFutureContributionFundIsin());
        applicationData.setPillar(mandateCommand.getPillar().toString());

        EpisX5RequestType request = episMessageFactory.createEpisX5RequestType();
        request.setDocumentDate(timeConverter.convert(mandateCommand.getCreatedDate()));
        request.setDocumentNumber(mandateCommand.getId().toString());
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setApplicationData(applicationData);

        EpisX5Type episX5Type = episMessageFactory.createEpisX5Type();
        episX5Type.setRequest(request);

        JAXBElement<EpisX5Type> fundContributionApplication = episMessageFactory.createVALIKUAVALDUS(episX5Type);

        String id = mandateCommand.getProcessId();

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
