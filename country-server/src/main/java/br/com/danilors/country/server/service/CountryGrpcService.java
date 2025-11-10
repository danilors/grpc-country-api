package br.com.danilors.country.server.service;

import br.com.danilors.country.AllCountriesRequest;
import br.com.danilors.country.CountryRequest;
import br.com.danilors.country.CountryResponse;
import br.com.danilors.country.CountryServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class CountryGrpcService extends CountryServiceGrpc.CountryServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(CountryGrpcService.class);

    private final CountryService countryService;

    public CountryGrpcService(CountryService countryService) {
        this.countryService = countryService;
    }

    @Override
    public void getCountry(CountryRequest request, StreamObserver<CountryResponse> responseObserver) {
        log.debug("Received getCountry request for code: {}", request.getCode());

        countryService.findById(request.getCode())
                .ifPresentOrElse(country -> {
                    log.info("Found country: {}", country.getDescription());
                    CountryResponse response = CountryResponse.newBuilder()
                            .setCode(country.getCode())
                            .setDescription(country.getDescription())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, () -> {
                    log.error("Country with code {} not found.", request.getCode());
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("Country with code " + request.getCode() + " not found.")
                            .asRuntimeException());
                });
    }

    @Override
    public void listAllCountries(AllCountriesRequest request, StreamObserver<CountryResponse> responseObserver) {
        log.info("Received request to list all countries");
        try {
            countryService.streamAll().forEach(country -> {
                CountryResponse response = CountryResponse.newBuilder()
                        .setCode(country.getCode())
                        .setDescription(country.getDescription())
                        .build();
                responseObserver.onNext(response);
            });
            responseObserver.onCompleted();
            log.info("Successfully streamed all countries");
        } catch (Exception e) {
            log.error("Error streaming countries", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("An error occurred while fetching countries.")
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
