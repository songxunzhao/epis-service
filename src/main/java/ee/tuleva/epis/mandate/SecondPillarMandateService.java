package ee.tuleva.epis.mandate;

import ee.tuleva.epis.config.ObjectFactoryConfiguration;
import ee.tuleva.epis.config.UserPrincipal;
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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecondPillarMandateService implements MandateService {

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
    public List<MandateResponse> sendMandate(UserPrincipal principal, MandateCommand mandateCommand) {
        ContactDetails contactDetails = contactDetailsService.getContactDetails(principal.getPersonalCode());
        Integer pillar = mandateCommand.getPillar();

        if (supports(pillar)) {
            return sendMandate(mandateCommand, contactDetails);
        }

        throw new IllegalStateException("Unsupported pillar: " + pillar);
    }

    @Override
    public boolean supports(Integer pillar) {
        return pillar == 2;
    }


    private List<MandateResponse> sendMandate(MandateCommand mandateCommand, ContactDetails contactDetails) {
        List<MandateResponse> mandateResponses = new ArrayList<>();

        if (mandateCommand.getFundTransferExchanges() != null) {
            List<MandateResponse> transferResponses = sendAllFundTransferApplications(
                mandateCommand, mapper(contactDetails, mandateCommand));
            mandateResponses.addAll(transferResponses);
        }

        if (mandateCommand.getFutureContributionFundIsin() != null) {
            MandateResponse selectionResponse = sendSelectionApplication(contactDetails, mandateCommand);
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

    private Function<List<FundTransferExchange>, MandateResponse> mapper(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        return exchanges -> {
            EpisMessage message = sendFundTransferApplicationQuery(
                contactDetails,
                exchanges,
                mandateCommand.getPillar(),
                mandateCommand.getCreatedDate(),
                mandateCommand.getId());
            EpisX6Type response = episMessageResponseStore.pop(message.getId(), EpisX6Type.class);
            return mandateResponseConverter.convert(response.getResponse(), message.getId());
        };
    }

    private EpisMessage sendFundTransferApplicationQuery(
        ContactDetails contactDetails, List<FundTransferExchange> fundTransferExchanges, Integer pillar,
        Instant createdDate, Long mandateId
    ) {
        PersonalData personalData = toPersonalDataConverter.convert(contactDetails);
        AddressType address = toAddressTypeConverter.convert(contactDetails);

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

        episService.send(episMessage);
        return episMessage;
    }

    private MandateResponse sendSelectionApplication(ContactDetails contactDetails,
                                                     MandateCommand mandateCommand) {
        EpisMessage message = sendSelectionApplicationQuery(contactDetails, mandateCommand);
        EpisX5Type response = episMessageResponseStore.pop(message.getId(), EpisX5Type.class);
        return mandateResponseConverter.convert(response.getResponse(), message.getId());
    }

    private EpisMessage sendSelectionApplicationQuery(
        ContactDetails contactDetails, MandateCommand mandateCommand) {
        PersonalData personalData = toPersonalDataConverter.convert(contactDetails);
        AddressType address = toAddressTypeConverter.convert(contactDetails);

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

        episService.send(episMessage);
        return episMessage;
    }

}
