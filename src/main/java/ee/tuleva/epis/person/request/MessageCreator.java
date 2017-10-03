package ee.tuleva.epis.person.request;

import org.springframework.stereotype.Service;

@Service
public class MessageCreator {

    public String getMessage(String personalCode) {

        String message = "<ISIKU_ANDMED>\n" +
                "         <Request>\n" +
                "                       <PersonalData PersonId=\"" + personalCode + "\"/>\n" +
                "         </Request>\n" +
                "      </ISIKU_ANDMED>";

        return message;
    }
}
