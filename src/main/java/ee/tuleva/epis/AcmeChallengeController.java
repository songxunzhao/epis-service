package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/FGkzMSy9-mRuD3ywjAfUd1S4988zv4L8x9lq_YPKclY")
	@ResponseBody
	public String challenge() {
		return "FGkzMSy9-mRuD3ywjAfUd1S4988zv4L8x9lq_YPKclY.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
