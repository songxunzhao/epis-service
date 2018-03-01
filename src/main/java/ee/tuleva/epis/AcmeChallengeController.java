package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/srJUCWylQEFu9io8xrVc-b9sgJKGPtmk-rv5Tgu4ges")
	@ResponseBody
	public String challenge() {
		return "srJUCWylQEFu9io8xrVc-b9sgJKGPtmk-rv5Tgu4ges.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
