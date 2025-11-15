
package com.example.movieservice.infrastructure.adapters.input.rest;

import com.example.movieservice.application.dto.movie.*;
import com.example.movieservice.domain.ports.input.RentalUseCase;
import com.example.movieservice.infrastructure.config.exceptions.GlobalExceptionHandler;
import com.example.movieservice.infrastructure.config.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = RentalController.class, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
@Import(GlobalExceptionHandler.class)
class RentalControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RentalUseCase rentalUseCase;

    private String rentalId;
    private String requestId;

    @BeforeEach
    void setUp() {
        rentalId = UUID.randomUUID().toString();
        requestId = UUID.randomUUID().toString();
    }

    @Test
    void createRental_shouldReturnCreated_whenSuccessful() {
        CreateRentalRequestContent request = CreateRentalRequestContent.builder()
                .movieId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .rentalDays(BigDecimal.valueOf(1))
                .build();
        CreateRentalResponseContent response = CreateRentalResponseContent.builder()
                .rentalId(rentalId)
                .build();
        when(rentalUseCase.create(any(CreateRentalRequestContent.class))).thenReturn(Mono.just(response));

        webTestClient.post().uri("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId)
                .body(Mono.just(request), CreateRentalRequestContent.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateRentalResponseContent.class)
                .value(res -> {
                    assert res.getRentalId().equals(rentalId);
                });
    }

    @Test
    void getRental_shouldReturnOk_whenRentalFound() {
        GetRentalResponseContent response = new GetRentalResponseContent();
        when(rentalUseCase.get(rentalId)).thenReturn(Mono.just(response));

        webTestClient.get().uri("/rentals/{rentalId}", rentalId)
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getRental_shouldReturnNotFound_whenRentalNotFound() {
        when(rentalUseCase.get(rentalId)).thenReturn(Mono.error(new NotFoundException("Rental not found")));

        webTestClient.get().uri("/rentals/{rentalId}", rentalId)
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateRental_shouldReturnOk_whenSuccessful() {
        UpdateRentalRequestContent request = UpdateRentalRequestContent.builder()
                .returnDate(Instant.now().toString())
                .build();
        UpdateRentalResponseContent response = new UpdateRentalResponseContent();
        when(rentalUseCase.update(any(String.class), any(UpdateRentalRequestContent.class))).thenReturn(Mono.just(response));

        webTestClient.put().uri("/rentals/{rentalId}", rentalId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId)
                .body(Mono.just(request), UpdateRentalRequestContent.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateRental_shouldReturnNotFound_whenRentalNotFound() {
        UpdateRentalRequestContent request = UpdateRentalRequestContent.builder()
                .returnDate(Instant.now().toString())
                .build();
        when(rentalUseCase.update(any(String.class), any(UpdateRentalRequestContent.class))).thenReturn(Mono.error(new NotFoundException("Rental not found")));

        webTestClient.put().uri("/rentals/{rentalId}", rentalId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId)
                .body(Mono.just(request), UpdateRentalRequestContent.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listRentals_shouldReturnOk_whenSuccessful() {
        ListRentalsResponseContent response = new ListRentalsResponseContent();
        when(rentalUseCase.list(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(response));

        webTestClient.get().uri("/rentals")
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listRentals_shouldReturnBadRequest_whenDateFromIsAfterDateTo() {
        String dateFrom = Instant.now().toString();
        String dateTo = Instant.now().minusSeconds(100).toString();
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/rentals")
                        .queryParam("dateFrom", dateFrom)
                        .queryParam("dateTo", dateTo)
                        .build())
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void listRentals_shouldReturnBadRequest_whenDateFormatIsInvalid() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/rentals")
                        .queryParam("dateFrom", "invalid-date")
                        .queryParam("dateTo", "invalid-date")
                        .build())
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
