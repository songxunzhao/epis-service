package ee.tuleva.epis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AcmeChallengeController {

	@RequestMapping(value = "/.well-known/acme-challenge/Z8Uf7u_ZLOAKf7OUtlspmvGuV-qDGW1OwkMclwe3M-4")
	@ResponseBody
	public String challenge() {
		return "Z8Uf7u_ZLOAKf7OUtlspmvGuV-qDGW1OwkMclwe3M-4.EMEBBxvSam3n_ien1J0z4dXeTuc2JuR3HqfAP6teLjE";
	}

}
