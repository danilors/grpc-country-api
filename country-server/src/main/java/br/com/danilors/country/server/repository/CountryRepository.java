package br.com.danilors.country.server.repository;

import br.com.danilors.country.server.domain.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface CountryRepository extends CrudRepository<Country, String> {
    @Query("SELECT c FROM Country c")
    Stream<Country> streamAll();
}
