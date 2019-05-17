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
import ee.x_road.epis.producer.ApplicationRequest2WithRedemptionUnitsType.ApplicationRows;
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
import java.util.function.Function;

import static java.util.Collections.singletonList;
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

        Integer pillar = mandateCommand.getPillar();

        if (pillar == 2) {
            return send2ndPillarMandate(mandateCommand, contactDetails);
        } else if (pillar == 3) {
            return send3rdPillarMandate(mandateCommand, contactDetails);
        }

        throw new IllegalStateException("Unsupported pillar" + pillar);
    }

    private List<MandateResponse> send3rdPillarMandate(
        MandateCommand mandateCommand, ContactDetails contactDetails) {
        List<MandateResponse> mandateResponses = new ArrayList<>();

        if (mandateCommand.getFundTransferExchanges() != null) {
            List<MandateResponse> transferResponses = sendAllFundTransferApplications(
                mandateCommand, thirdPillarMapper(contactDetails, mandateCommand));
            mandateResponses.addAll(transferResponses);
        }

        if (mandateCommand.getFutureContributionFundIsin() != null) {
            List<MandateResponse> selectionResponses = send3rdPillarSelectionApplication(contactDetails,
                mandateCommand);
            mandateResponses.addAll(selectionResponses);
        }

        return mandateResponses;
    }

    private List<MandateResponse> send3rdPillarSelectionApplication(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        EpisMessage message = send3rdPillarSelectionApplicationQuery(contactDetails, mandateCommand);
        EpisX31Type response = episMessageResponseStore.pop(message.getId(), EpisX31Type.class);
        MandateResponse mandateResponse = mandateResponseConverter.convert(response.getResponse(), message.getId());
        return singletonList(mandateResponse);
    }

    private EpisMessage send3rdPillarSelectionApplicationQuery(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        PersonalData personalData = toPersonalData(contactDetails);
        AddressType address = toAddress(contactDetails);

        EpisX31RequestType.ApplicationData.ApplicationRow row = new EpisX31RequestType.ApplicationData.ApplicationRow();
        row.setDestinationISIN(mandateCommand.getFutureContributionFundIsin());
        row.setPercentage(new BigDecimal(100));

        EpisX31RequestType.ApplicationData applicationData = episMessageFactory.createEpisX31RequestTypeApplicationData();
        applicationData.getApplicationRow().add(row);
        applicationData.setPillar(mandateCommand.getPillar().toString());

        EpisX31RequestType request = episMessageFactory.createEpisX31RequestType();
        request.setDocumentDate(timeConverter.convert(mandateCommand.getCreatedDate()));
        request.setDocumentNumber(mandateCommand.getId().toString());
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setApplicationData(applicationData);

        EpisX31Type EpisX31Type = episMessageFactory.createEpisX31Type();
        EpisX31Type.setRequest(request);

        JAXBElement<EpisX31Type> fundContributionApplication = episMessageFactory.createVALIKUAVALDUSRIDADEGA(EpisX31Type);

        String id = mandateCommand.getProcessId();

        Ex ex = episMessageWrapper.wrap(id, fundContributionApplication);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

    private List<MandateResponse> send2ndPillarMandate(MandateCommand mandateCommand, ContactDetails contactDetails) {
        List<MandateResponse> mandateResponses = new ArrayList<>();

        if (mandateCommand.getFundTransferExchanges() != null) {
            List<MandateResponse> transferResponses = sendAllFundTransferApplications(
                mandateCommand, secondPillarMapper(contactDetails, mandateCommand));
            mandateResponses.addAll(transferResponses);
        }

        if (mandateCommand.getFutureContributionFundIsin() != null) {
            MandateResponse selectionResponse = send2ndPillarSelectionApplication(contactDetails, mandateCommand);
            mandateResponses.add(selectionResponse);
        }

        return mandateResponses;
    }

    private List<MandateResponse> sendAllFundTransferApplications(
        MandateCommand mandateCommand, Function<List<FundTransferExchange>, MandateResponse> mapper) {
        return mandateCommand.getFundTransferExchanges().stream()
            .collect(groupingBy(FundTransferExchange::getSourceFundIsin)).values().stream()
            .map(mapper)
            .collect(toList());
    }

    private Function<List<FundTransferExchange>, MandateResponse> secondPillarMapper(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        return exchanges -> {
            EpisMessage message = send2ndPillarFundTransferApplicationQuery(
                contactDetails,
                exchanges,
                mandateCommand.getPillar(),
                mandateCommand.getCreatedDate(),
                mandateCommand.getId());
            EpisX6Type response = episMessageResponseStore.pop(message.getId(), EpisX6Type.class);
            return mandateResponseConverter.convert(response.getResponse(), message.getId());
        };
    }

    private Function<List<FundTransferExchange>, MandateResponse> thirdPillarMapper(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        return exchanges -> {
            EpisMessage message = send3rdPillarFundTransferApplicationQuery(
                contactDetails,
                exchanges,
                mandateCommand.getPillar(),
                mandateCommand.getCreatedDate(),
                mandateCommand.getId()
            );
            EpisX37Type response = episMessageResponseStore.pop(message.getId(), EpisX37Type.class);
            return mandateResponseConverter.convert(response.getResponse(), message.getId());
        };
    }

    private EpisMessage send3rdPillarFundTransferApplicationQuery(
        ContactDetails contactDetails, List<FundTransferExchange> fundTransferExchanges, Integer pillar,
        Instant createdDate, Long mandateId
    ) {
        PersonalData personalData = toPersonalData(contactDetails);
        AddressType address = toAddress(contactDetails);

        EpisX37RequestType.ApplicationData applicationData =
            episMessageFactory.createEpisX37RequestTypeApplicationData();

        ApplicationRows applicationRows = new ApplicationRows();
        fundTransferExchanges.stream()
            .collect(groupingBy(FundTransferExchange::getSourceFundIsin))
            .forEach((isin, exchanges) -> {
                exchanges.forEach(exchange -> {
                    ApplicationRows.ApplicationRow row = new ApplicationRows.ApplicationRow();
                    row.setISIN(exchange.getTargetFundIsin());
                    row.setUnitAmount(exchange.getAmount());
                    applicationRows.getApplicationRow().add(row);
                });
                applicationData.setSourceISIN(isin);
                applicationData.setPillar(pillar.toString());
            });


        EpisX37RequestType request = episMessageFactory.createEpisX37RequestType();
        request.setDocumentDate(timeConverter.convert(createdDate));
        request.setDocumentNumber(mandateId.toString());
        request.setPersonalData(personalData);
        request.setAddress(address);
        request.setApplicationData(applicationData);
        request.setApplicationRows(applicationRows);

        EpisX37Type episX37Type = episMessageFactory.createEpisX37Type();
        episX37Type.setRequest(request);

        JAXBElement<EpisX37Type> fundTransferApplication =
            episMessageFactory.createOSAKUTEVAHETAMISEAVALDUS3(episX37Type);

        String id = fundTransferExchanges.get(0).getProcessId();

        Ex ex = episMessageWrapper.wrap(id, fundTransferApplication);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

    private EpisMessage send2ndPillarFundTransferApplicationQuery(
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

    private MandateResponse send2ndPillarSelectionApplication(ContactDetails contactDetails,
                                                              MandateCommand mandateCommand) {
        EpisMessage message = send2ndPillarSelectionApplicationQuery(contactDetails, mandateCommand);
        EpisX5Type response = episMessageResponseStore.pop(message.getId(), EpisX5Type.class);
        return mandateResponseConverter.convert(response.getResponse(), message.getId());
    }

    private EpisMessage send2ndPillarSelectionApplicationQuery(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
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
