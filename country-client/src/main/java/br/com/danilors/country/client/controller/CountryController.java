package br.com.danilors.country.client.controller;

import br.com.danilors.country.client.dto.Country;
import br.com.danilors.country.client.service.CountryService;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/countries")
public class CountryController {

    private static final Logger log = LoggerFactory.getLogger(CountryController.class);

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("/{code}")
    public Country getContryByCode(@PathVariable("code") String code) {
        log.info("Request received for country with code: {}", code);
        return countryService.getCountry(code);
    }

    @GetMapping
    public Flowable<Country> listAllCountries() {
        log.info("Request received to list all countries");
        return countryService.listAllCountries();
    }
}
