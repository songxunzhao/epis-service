package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/gENnwF_sAgpURTeZYyEzUypwz5S4vUcwD_bgefbQ73U")
	@ResponseBody
	public String challenge() {
		return "gENnwF_sAgpURTeZYyEzUypwz5S4vUcwD_bgefbQ73U.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
