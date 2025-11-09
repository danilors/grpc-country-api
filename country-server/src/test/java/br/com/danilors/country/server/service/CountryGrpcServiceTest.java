package br.com.danilors.country.server.service;

import br.com.danilors.country.AllCountriesRequest;
import br.com.danilors.country.CountryRequest;
import br.com.danilors.country.CountryResponse;
import br.com.danilors.country.server.domain.Country;
import br.com.danilors.country.server.repository.CountryRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryGrpcServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private StreamObserver<CountryResponse> responseObserver;

    @InjectMocks
    private CountryGrpcService countryGrpcService;

    @Captor
    private ArgumentCaptor<CountryResponse> responseCaptor;

    @Captor
    private ArgumentCaptor<StatusRuntimeException> errorCaptor;

    @Test
    @DisplayName("Should return a country when a valid code is provided")
    void getCountry_whenCountryExists_shouldReturnCountry() {
        String countryCode = "BR";
        Country country = new Country(countryCode, "Brazil");
        when(countryRepository.findById(countryCode)).thenReturn(Optional.of(country));

        CountryRequest request = CountryRequest.newBuilder().setCode(countryCode).build();
        countryGrpcService.getCountry(request, responseObserver);

        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        CountryResponse response = responseCaptor.getValue();
        assertEquals(countryCode, response.getCode());
        assertEquals("Brazil", response.getDescription());
    }

    @Test
    @DisplayName("Should return NOT_FOUND when country does not exist")
    void getCountry_whenCountryDoesNotExist_shouldReturnNotFound() {
        String countryCode = "XX";
        when(countryRepository.findById(countryCode)).thenReturn(Optional.empty());

        CountryRequest request = CountryRequest.newBuilder().setCode(countryCode).build();
        countryGrpcService.getCountry(request, responseObserver);

        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = errorCaptor.getValue();
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
        assertEquals("Country with code " + countryCode + " not found.", exception.getStatus().getDescription());
    }

    @Test
    @DisplayName("Should stream all countries successfully")
    void listAllCountries_whenCountriesExist_shouldStreamAll() {
        List<Country> countries = List.of(
                new Country("BR", "Brazil"),
                new Country("US", "United States")
        );
        when(countryRepository.findAll()).thenReturn(countries);

        AllCountriesRequest request = AllCountriesRequest.newBuilder().build();
        countryGrpcService.listAllCountries(request, responseObserver);

        verify(responseObserver, times(2)).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        List<CountryResponse> responses = responseCaptor.getAllValues();
        assertEquals(2, responses.size());
        assertEquals("BR", responses.get(0).getCode());
        assertEquals("US", responses.get(1).getCode());
    }

    @Test
    @DisplayName("Should return INTERNAL error when repository fails")
    void listAllCountries_whenRepositoryThrowsException_shouldReturnInternalError() {
        when(countryRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        AllCountriesRequest request = AllCountriesRequest.newBuilder().build();
        countryGrpcService.listAllCountries(request, responseObserver);

        verify(responseObserver).onError(any(StatusRuntimeException.class));
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
}