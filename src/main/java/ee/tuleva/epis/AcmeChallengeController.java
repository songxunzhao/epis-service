package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/H9fmdF19meluzw4WOOOuJGyAzv0mNKDPq66aHNFkNwE")
	@ResponseBody
	public String challenge() {
		return "H9fmdF19meluzw4WOOOuJGyAzv0mNKDPq66aHNFkNwE.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
