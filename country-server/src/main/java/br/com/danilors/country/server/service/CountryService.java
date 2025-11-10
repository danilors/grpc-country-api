package br.com.danilors.country.server.service;

import br.com.danilors.country.server.domain.Country;
import br.com.danilors.country.server.repository.CountryRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
public class CountryService {

    private final CountryRepository countryRepository;

    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Cacheable("countries")
    public Optional<Country> findById(String code) {
        return countryRepository.findById(code);
    }

    @Transactional(readOnly = true)
    public List<Country> streamAll() {
        return countryRepository.streamAll().toList();
    }
}
