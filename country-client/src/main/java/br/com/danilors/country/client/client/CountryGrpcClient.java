package br.com.danilors.country.client.client;

import br.com.danilors.country.CountryRequest;
import br.com.danilors.country.CountryResponse;
import br.com.danilors.country.CountryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CountryGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(CountryGrpcClient.class);

    @Value("${grpc.server.host}")
    private String host;

    @Value("${grpc.server.port}")
    private int port;

    private ManagedChannel channel;
    private CountryServiceGrpc.CountryServiceBlockingStub blockingStub;

    @PostConstruct
    private void init() {
        log.info("Initializing gRPC client for server at {}:{}", host, port);
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = CountryServiceGrpc.newBlockingStub(channel);
    }

    public CountryResponse getCountry(String countryCode) {
        CountryRequest request = CountryRequest.newBuilder()
                .setCode(countryCode)
                .build();
        log.info("Sending gRPC request: {}", request);
        return blockingStub.getCountry(request);
    }
}
