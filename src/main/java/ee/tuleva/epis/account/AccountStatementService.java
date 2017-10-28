package ee.tuleva.epis.account;

import ee.tuleva.epis.contact.ContactDetailsService;
import ee.tuleva.epis.epis.EpisMessageWrapper;
import ee.tuleva.epis.epis.EpisService;
import ee.tuleva.epis.epis.request.EpisMessage;
import ee.tuleva.epis.epis.response.EpisMessageResponseStore;
import ee.x_road.epis.producer.EpisX14RequestType;
import ee.x_road.epis.producer.EpisX14Type;
import ee.x_road.epis.producer.PersonDataRequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mhub.xsd.envelope._01.Ex;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountStatementService {

    private final EpisService episService;
    private final EpisMessageResponseStore episMessageResponseStore;
    private final EpisMessageWrapper episMessageWrapper;
    private final ContactDetailsService contactDetailsService;
    private final EpisX14TypeToFundBalanceListConverter converter;

    List<FundBalance> get(String personalCode) {
        EpisMessage message = sendQuery(personalCode);
        EpisX14Type response = episMessageResponseStore.pop(message.getId(), EpisX14Type.class);

        return markActiveFund(converter.convert(response), personalCode);
    }

    private List<FundBalance> markActiveFund(List<FundBalance> fundBalanceList, String personalCode) {
        String activeFundIsin = contactDetailsService.get(personalCode).getActiveSecondPillarFundIsin();

        return fundBalanceList.stream().map(fundBalance -> {
            if(fundBalance.getIsin().equalsIgnoreCase(activeFundIsin)) {
                fundBalance.setActiveContributions(true);
            }
            return fundBalance;
        }).collect(Collectors.toList());
    }

    private EpisMessage sendQuery(String personalCode) {
        ee.x_road.epis.producer.ObjectFactory episFactory = new ee.x_road.epis.producer.ObjectFactory();

        PersonDataRequestType personalData = episFactory.createPersonDataRequestType();
        personalData.setPersonId(personalCode);

        EpisX14RequestType request = episFactory.createEpisX14RequestType();
        request.setPersonalData(personalData);

        EpisX14Type episX14Type = episFactory.createEpisX14Type();
        episX14Type.setRequest(request);

        JAXBElement<EpisX14Type> personalDataRequest = episFactory.createKONTOVALJAVOTE(episX14Type);

        String id = UUID.randomUUID().toString().replace("-", "");
        Ex ex = episMessageWrapper.wrap(id, personalDataRequest);

        EpisMessage episMessage = EpisMessage.builder()
            .payload(ex)
            .id(id)
            .build();

        episService.send(episMessage.getPayload());
        return episMessage;
    }

}
