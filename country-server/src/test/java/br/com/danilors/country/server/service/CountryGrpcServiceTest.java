package br.com.danilors.country.server.service;

import br.com.danilors.country.AllCountriesRequest;
import br.com.danilors.country.CountryRequest;
import br.com.danilors.country.CountryResponse;
import br.com.danilors.country.server.domain.Country;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryGrpcServiceTest {

    @Mock
    private CountryService countryService; // Mock the service layer, not the repository

    @Mock
    private StreamObserver<CountryResponse> responseObserver;

    @Captor
    private ArgumentCaptor<CountryResponse> responseCaptor;

    @Captor
    private ArgumentCaptor<StatusRuntimeException> errorCaptor;

    private CountryGrpcService countryGrpcService;

    @BeforeEach
    void setUp() {
        // Manually instantiate the gRPC service with the mocked business service
        countryGrpcService = new CountryGrpcService(countryService);
    }

    @Test
    @DisplayName("getCountry: Should return a country when a valid code is provided")
    void getCountry_whenCountryExists_shouldReturnCountry() {
        // Arrange
        String countryCode = "BR";
        Country country = new Country(countryCode, "Brazil");
        when(countryService.findById(countryCode)).thenReturn(Optional.of(country)); // Stub the service method
        CountryRequest request = CountryRequest.newBuilder().setCode(countryCode).build();

        // Act
        countryGrpcService.getCountry(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        CountryResponse response = responseCaptor.getValue();
        assertEquals(countryCode, response.getCode());
        assertEquals("Brazil", response.getDescription());
    }

    @Test
    @DisplayName("getCountry: Should return NOT_FOUND when country does not exist")
    void getCountry_whenCountryDoesNotExist_shouldReturnNotFound() {
        // Arrange
        String countryCode = "XX";
        when(countryService.findById(countryCode)).thenReturn(Optional.empty()); // Stub the service method
        CountryRequest request = CountryRequest.newBuilder().setCode(countryCode).build();

        // Act
        countryGrpcService.getCountry(request, responseObserver);

        // Assert
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = errorCaptor.getValue();
        assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode());
    }

    @Test
    @DisplayName("listAllCountries: Should stream all countries successfully")
    void listAllCountries_whenCountriesExist_shouldStreamAll() {
        // Arrange
        List<Country> countries = List.of(new Country("BR", "Brazil"), new Country("US", "United States"));
        when(countryService.streamAll()).thenReturn(countries); // Stub the service method
        AllCountriesRequest request = AllCountriesRequest.getDefaultInstance();

        // Act
        countryGrpcService.listAllCountries(request, responseObserver);

        // Assert
        verify(responseObserver, times(2)).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        List<CountryResponse> responses = responseCaptor.getAllValues();
        assertEquals(2, responses.size());
        assertEquals("BR", responses.get(0).getCode());
        assertEquals("US", responses.get(1).getCode());
    }

    @Test
    @DisplayName("listAllCountries: Should return INTERNAL error when service throws an exception")
    void listAllCountries_whenServiceThrowsException_shouldReturnInternalError() {
        // Arrange
        when(countryService.streamAll()).thenThrow(new RuntimeException("Service layer error")); // Stub the service method
        AllCountriesRequest request = AllCountriesRequest.getDefaultInstance();

        // Act
        countryGrpcService.listAllCountries(request, responseObserver);

        // Assert
        verify(responseObserver).onError(errorCaptor.capture());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();

        StatusRuntimeException exception = errorCaptor.getValue();
        assertEquals(Status.INTERNAL.getCode(), exception.getStatus().getCode());
        assertEquals("An error occurred while fetching countries.", exception.getStatus().getDescription());
    }
}
