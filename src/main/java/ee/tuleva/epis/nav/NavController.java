package ee.tuleva.epis.nav;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class NavController {

    private final NavService navService;

    @ApiOperation(value = "Get a NAV of a fund on a date")
    @GetMapping("/navs/{isin}")
    public NavData getNav(@PathVariable String isin, @RequestParam @DateTimeFormat(iso = DATE) LocalDate date) {
        if (date == null)
            throw new ResponseStatusException(BAD_REQUEST, "Missing date");
        return navService.getNavData(isin, date)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }
}
