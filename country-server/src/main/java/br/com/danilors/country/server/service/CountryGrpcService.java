package br.com.danilors.country.server.service;



import br.com.danilors.country.CountryRequest;
import br.com.danilors.country.CountryResponse;
import br.com.danilors.country.CountryServiceGrpc;
import br.com.danilors.country.server.domain.Country;
import br.com.danilors.country.server.repository.CountryRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.Optional;

@GrpcService
public class CountryGrpcService extends CountryServiceGrpc.CountryServiceImplBase {

    private final CountryRepository countryRepository;

    public CountryGrpcService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public void getCountry(CountryRequest request, StreamObserver<CountryResponse> responseObserver) {
        countryRepository.findById(request.getCode())
                .ifPresentOrElse(country -> {
                    CountryResponse response = CountryResponse.newBuilder()
                            .setCode(country.getCode())
                            .setDescription(country.getDescription())
                            .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }, () -> responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Country with code " + request.getCode() + " not found.")
                        .asRuntimeException()));
    }
}
