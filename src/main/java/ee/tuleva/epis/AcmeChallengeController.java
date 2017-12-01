package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/LQfKd5vcdZI0ipIHxCCHZ7Xjb84_ugMWZiKaDFUc0gM")
	@ResponseBody
	public String challenge() {
		return "LQfKd5vcdZI0ipIHxCCHZ7Xjb84_ugMWZiKaDFUc0gM.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
