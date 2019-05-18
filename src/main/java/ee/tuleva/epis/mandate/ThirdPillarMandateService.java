package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.ObjectFactoryConfiguration;
import ee.tuleva.epis.contact.ContactDetails;
import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.converter.ContactDetailsToAddressTypeConverter;
import ee.tuleva.epis.epis.converter.ContactDetailsToPersonalDataConverter;
import ee.tuleva.epis.epis.converter.InstantToXmlGregorianCalendarConverter;
import ee.tuleva.epis.epis.converter.MandateResponseConverter;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.request.EpisMessageWrapper;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.tuleva.epis.mandate.application.FundTransferExchange;
import ee.x_road.epis.producer.*;
import ee.x_road.epis.producer.ApplicationRequest2WithRedemptionUnitsType.ApplicationRows;
import ee.x_road.epis.producer.ApplicationRequestType.PersonalData;
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
public class ThirdPillarMandateService implements MandateService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ObjectFactoryConfiguration.EpisMessageFactory episMessageFactory;
    private final InstantToXmlGregorianCalendarConverter timeConverter;
    private final ContactDetailsService contactDetailsService;
    private final MandateResponseConverter mandateResponseConverter;
    private final ContactDetailsToPersonalDataConverter toPersonalDataConverter;
    private final ContactDetailsToAddressTypeConverter toAddressTypeConverter;

    @Override
    public List<MandateResponse> sendMandate(String personalCode, MandateCommand mandateCommand) {
        ContactDetails contactDetails = contactDetailsService.get(personalCode);

        Integer pillar = mandateCommand.getPillar();

         if (supports(pillar)) {
            return sendMandate(mandateCommand, contactDetails);
        }

        throw new IllegalStateException("Unsupported pillar: " + pillar);
    }

    @Override
    public boolean supports(Integer pillar) {
        return pillar == 3;
    }

    private List<MandateResponse> sendMandate(
        MandateCommand mandateCommand, ContactDetails contactDetails) {
        List<MandateResponse> mandateResponses = new ArrayList<>();

        if (mandateCommand.getFundTransferExchanges() != null) {
            List<MandateResponse> transferResponses = sendAllFundTransferApplications(
                mandateCommand, mapper(contactDetails, mandateCommand));
            mandateResponses.addAll(transferResponses);
        }

        if (mandateCommand.getFutureContributionFundIsin() != null) {
            List<MandateResponse> selectionResponses = sendSelectionApplication(contactDetails,
                mandateCommand);
            mandateResponses.addAll(selectionResponses);
        }

        return mandateResponses;
    }

    private List<MandateResponse> sendSelectionApplication(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        EpisMessage message = sendSelectionApplicationQuery(contactDetails, mandateCommand);
        EpisX31Type response = episMessageResponseStore.pop(message.getId(), EpisX31Type.class);
        MandateResponse mandateResponse = mandateResponseConverter.convert(response.getResponse(), message.getId());
        return singletonList(mandateResponse);
    }

    private EpisMessage sendSelectionApplicationQuery(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        PersonalData personalData = toPersonalDataConverter.convert(contactDetails);
        AddressType address = toAddressTypeConverter.convert(contactDetails);

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

        episService.send(episMessage);
        return episMessage;
    }

    private List<MandateResponse> sendAllFundTransferApplications(
        MandateCommand mandateCommand, Function<List<FundTransferExchange>, MandateResponse> mapper) {
        return mandateCommand.getFundTransferExchanges().stream()
            .collect(groupingBy(FundTransferExchange::getSourceFundIsin)).values().stream()
            .map(mapper)
            .collect(toList());
    }

    private Function<List<FundTransferExchange>, MandateResponse> mapper(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        return exchanges -> {
            EpisMessage message = sendFundTransferApplicationQuery(
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

    private EpisMessage sendFundTransferApplicationQuery(
        ContactDetails contactDetails, List<FundTransferExchange> fundTransferExchanges, Integer pillar,
        Instant createdDate, Long mandateId
    ) {
        PersonalData personalData = toPersonalDataConverter.convert(contactDetails);
        AddressType address = toAddressTypeConverter.convert(contactDetails);

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

        episService.send(episMessage);
        return episMessage;
    }

}
