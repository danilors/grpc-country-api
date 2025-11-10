package br.com.danilors.country.client;

import br.com.danilors.country.AllCountriesRequest;
import br.com.danilors.country.CountryRequest;
import br.com.danilors.country.CountryResponse;
import br.com.danilors.country.CountryServiceGrpc;
import br.com.danilors.country.client.client.CountryGrpcClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.reactivex.rxjava3.subscribers.TestSubscriber; // Added this import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountryGrpcClientTest {

    @InjectMocks
    private CountryGrpcClient countryGrpcClient;

    @Mock
    private CountryServiceGrpc.CountryServiceBlockingStub blockingStub;

    @Mock
    private CountryServiceGrpc.CountryServiceStub asyncStub;

    @Mock
    private ManagedChannel managedChannel;

    @Captor
    private ArgumentCaptor<StreamObserver<CountryResponse>> streamObserverCaptor;

    @BeforeEach
    void setUp() {
        // Inject @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(countryGrpcClient, "host", "localhost");
        ReflectionTestUtils.setField(countryGrpcClient, "port", 9090);

        // Mock the ManagedChannelBuilder and newBlockingStub/newStub calls
        try (MockedStatic<ManagedChannelBuilder> mockedBuilder = Mockito.mockStatic(ManagedChannelBuilder.class)) {
            ManagedChannelBuilder builder = Mockito.mock(ManagedChannelBuilder.class);
            mockedBuilder.when(() -> ManagedChannelBuilder.forAddress(any(String.class), any(Integer.class)))
                    .thenReturn(builder);
            when(builder.usePlaintext()).thenReturn(builder);
            when(builder.build()).thenReturn(managedChannel);

            // Call the @PostConstruct method manually
            countryGrpcClient.init();
        }

        // IMPORTANT: Inject our mocked stubs into the client instance after init()
        ReflectionTestUtils.setField(countryGrpcClient, "blockingStub", blockingStub);
        ReflectionTestUtils.setField(countryGrpcClient, "asyncStub", asyncStub);
    }

    @Test
    void getCountry_shouldReturnCountryResponse() {
        // Given
        String countryCode = "USA"; // Changed to "USA" for consistency with expected response
        CountryResponse expectedResponse = CountryResponse.newBuilder()
                .setDescription("United States")
                .setCode(countryCode)
                .build();

        when(blockingStub.getCountry(any(CountryRequest.class))).thenReturn(expectedResponse);

        // When
        CountryResponse actualResponse = countryGrpcClient.getCountry(countryCode);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        ArgumentCaptor<CountryRequest> requestCaptor = ArgumentCaptor.forClass(CountryRequest.class);
        verify(blockingStub).getCountry(requestCaptor.capture());
        assertEquals(countryCode, requestCaptor.getValue().getCode());
    }

    @Test
    void listAllCountries_shouldReturnFlowableOfCountryResponses() {
        // Given
        CountryResponse country1 = CountryResponse.newBuilder().setDescription("USA").setCode("USA").build();
        CountryResponse country2 = CountryResponse.newBuilder().setDescription("Canada").setCode("CAN").build();

        // When
        // Capture the TestSubscriber from the first call
        TestSubscriber<CountryResponse> testSubscriber = countryGrpcClient.listAllCountries().test();

        testSubscriber
                .assertNoErrors()
                .assertNotComplete()
                .assertValueCount(0); // No values yet, as we haven't triggered onNext

        // Verify that listAllCountries was called and capture the StreamObserver
        verify(asyncStub).listAllCountries(any(AllCountriesRequest.class), streamObserverCaptor.capture());
        StreamObserver<CountryResponse> actualObserver = streamObserverCaptor.getValue();

        // Simulate server sending responses
        actualObserver.onNext(country1);
        actualObserver.onNext(country2);
        actualObserver.onCompleted();

        // Then
        // Now assert on the *same* testSubscriber that received the values
        testSubscriber
                .assertValues(country1, country2)
                .assertComplete();
    }
}
