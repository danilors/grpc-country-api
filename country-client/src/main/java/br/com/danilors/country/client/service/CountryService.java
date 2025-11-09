package br.com.danilors.country.client.service;

import br.com.danilors.country.client.client.CountryGrpcClient;
import br.com.danilors.country.client.dto.Country;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CountryService {

    private static final Logger log = LoggerFactory.getLogger(CountryService.class);

    private final CountryGrpcClient countryGrpcClient;

    public CountryService(CountryGrpcClient countryGrpcClient) {
        this.countryGrpcClient = countryGrpcClient;
    }



    public Country getCountry(String countryCode) {
        log.info("Calling gRPC client for country with code: {}", countryCode);
        var countryResponse = countryGrpcClient.getCountry(countryCode);
        log.info("gRPC client returned country: {}", countryResponse);
        return new Country(countryResponse.getCode(), countryResponse.getDescription());
    }

    public Flowable<Country> listAllCountries() {
        log.info("Calling gRPC client to list all countries");
        return countryGrpcClient.listAllCountries()
                .map(countryResponse -> new Country(countryResponse.getCode(), countryResponse.getDescription()));
    }
}
