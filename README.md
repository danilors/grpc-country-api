# gRPC Country API

This project demonstrates a simple client-server application using gRPC with Spring Boot.

## Project Structure

The project is divided into two main modules:

- `country-server`: A gRPC server that provides information about countries.
- `country-client`: A Spring Boot application that consumes the gRPC service and exposes a REST endpoint.

## Prerequisites

- Java 17 or higher
- Maven
- Docker

## Running the Application

### 1. Run the `country-server`

Navigate to the `country-server` directory and run the application using Maven:

```bash
cd country-server
./mvnw spring-boot:run
```

The gRPC server will start on port `9090`.

### 2. Run the `country-client`

Navigate to the `country-client` directory and run the application using Maven:

```bash
cd country-client
./mvnw spring-boot:run
```

The REST API will be available on port `8080`.

## Running with Docker

You can also run the application using Docker. First, build the JAR files for both `country-server` and `country-client` by running the following command in the root directory:

```bash
./mvnw clean install
```

Then, use Docker Compose to build and run the services:

```bash
docker-compose up --build
```

The services will be available at the same ports as when running them directly.

## API Usage

### gRPC API

You can use `grpcurl` to interact with the gRPC API.

#### Get a single country

```bash
grpcurl -plaintext -d '{"code": "DJ"}' localhost:9090 country.CountryService/getCountry
```

**Output:**

```json
{
  "code": "DJ",
  "description": "Djibouti"
}
```

#### List all countries

```bash
grpcurl -plaintext -d '{}' localhost:9090 country.CountryService/listAllCountries
```

**Output:**

```json
{
  "code": "BR",
  "description": "Brazil"
}
{
  "code": "CN",
  "description": "China"
}
...
```

### REST API

The `country-client` exposes a REST endpoint to get a country by its code.

#### Get a single country

```bash
curl http://localhost:8080/countries/BR
```

**Output:**

```json
{
  "code": "BR",
  "description": "Brazil"
}
```
