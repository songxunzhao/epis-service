package ee.tuleva.epis.fund;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class FundController {

  private final FundService fundService;

  @ApiOperation(value = "Get a list of pension funds")
  @GetMapping("/funds")
  public List<Fund> getPensionFunds() {
    return fundService.getPensionFunds();
  }

}
