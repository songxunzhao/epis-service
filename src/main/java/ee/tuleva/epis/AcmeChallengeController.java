package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/FFdNE25rVBK_ZCy13dp2viktltzVs3FPCM-BCjQNUjU")
	@ResponseBody
	public String challenge() {
		return "FFdNE25rVBK_ZCy13dp2viktltzVs3FPCM-BCjQNUjU.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
