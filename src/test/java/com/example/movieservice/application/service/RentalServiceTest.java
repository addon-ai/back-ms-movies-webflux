
package com.example.movieservice.application.service;

import com.example.movieservice.application.dto.movie.*;
import com.example.movieservice.application.mapper.RentalMapper;
import com.example.movieservice.domain.model.Rental;
import com.example.movieservice.domain.ports.output.RentalRepositoryPort;
import com.example.movieservice.infrastructure.config.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepositoryPort rentalRepositoryPort;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalService rentalService;

    private Rental rental;
    private String rentalId;

    @BeforeEach
    void setUp() {
        rentalId = UUID.randomUUID().toString();
        rental = Rental.builder().rentalId(rentalId).build();
    }

    @Test
    void create_shouldReturnCreateRentalResponseContent_whenSuccessful() {
        CreateRentalRequestContent request = new CreateRentalRequestContent();
        CreateRentalResponseContent response = new CreateRentalResponseContent();

        when(rentalMapper.fromCreateRequest(any(CreateRentalRequestContent.class))).thenReturn(rental);
        when(rentalRepositoryPort.save(any(Rental.class))).thenReturn(Mono.just(rental));
        when(rentalMapper.toCreateResponse(any(Rental.class))).thenReturn(response);

        StepVerifier.create(rentalService.create(request))
                .expectNext(response)
                .verifyComplete();

        verify(rentalMapper).fromCreateRequest(request);
        verify(rentalRepositoryPort).save(rental);
        verify(rentalMapper).toCreateResponse(rental);
    }

    @Test
    void create_shouldReturnError_whenRepositoryFails() {
        CreateRentalRequestContent request = new CreateRentalRequestContent();
        when(rentalMapper.fromCreateRequest(any(CreateRentalRequestContent.class))).thenReturn(rental);
        when(rentalRepositoryPort.save(any(Rental.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalService.create(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void get_shouldReturnGetRentalResponseContent_whenRentalFound() {
        GetRentalResponseContent response = new GetRentalResponseContent();
        when(rentalRepositoryPort.findById(rentalId)).thenReturn(Mono.just(rental));
        when(rentalMapper.toGetResponse(any(Rental.class))).thenReturn(response);

        StepVerifier.create(rentalService.get(rentalId))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void get_shouldThrowNotFoundException_whenRentalNotFound() {
        when(rentalRepositoryPort.findById(rentalId)).thenReturn(Mono.empty());

        StepVerifier.create(rentalService.get(rentalId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void get_shouldReturnError_whenRepositoryFails() {
        when(rentalRepositoryPort.findById(rentalId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalService.get(rentalId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void update_shouldReturnUpdateRentalResponseContent_whenSuccessful() {
        UpdateRentalRequestContent request = new UpdateRentalRequestContent();
        UpdateRentalResponseContent response = new UpdateRentalResponseContent();

        when(rentalRepositoryPort.findById(rentalId)).thenReturn(Mono.just(rental));
        doNothing().when(rentalMapper).updateEntityFromRequest(any(UpdateRentalRequestContent.class), any(Rental.class));
        when(rentalRepositoryPort.save(any(Rental.class))).thenReturn(Mono.just(rental));
        when(rentalMapper.toUpdateResponse(any(Rental.class))).thenReturn(response);

        StepVerifier.create(rentalService.update(rentalId, request))
                .expectNext(response)
                .verifyComplete();

        verify(rentalMapper).updateEntityFromRequest(request, rental);
        verify(rentalRepositoryPort).save(rental);
        verify(rentalMapper).toUpdateResponse(rental);
    }

    @Test
    void update_shouldThrowNotFoundException_whenRentalNotFound() {
        UpdateRentalRequestContent request = new UpdateRentalRequestContent();
        when(rentalRepositoryPort.findById(rentalId)).thenReturn(Mono.empty());

        StepVerifier.create(rentalService.update(rentalId, request))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void update_shouldReturnError_whenRepositoryFails() {
        UpdateRentalRequestContent request = new UpdateRentalRequestContent();
        when(rentalRepositoryPort.findById(rentalId)).thenReturn(Mono.just(rental));
        doNothing().when(rentalMapper).updateEntityFromRequest(any(UpdateRentalRequestContent.class), any(Rental.class));
        when(rentalRepositoryPort.save(any(Rental.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalService.update(rentalId, request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void list_shouldReturnListRentalsResponseContent_whenSuccessful() {
        ListRentalsResponseContent response = new ListRentalsResponseContent();
        when(rentalRepositoryPort.findByFilters(any(), any(), any(), any(), any(), any())).thenReturn(Flux.just(rental));
        when(rentalMapper.toListResponse(any(), anyInt(), anyInt())).thenReturn(response);

        StepVerifier.create(rentalService.list(1, 10, "Test", "ACTIVE", "2023-01-01", "2023-12-31"))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void list_shouldReturnListRentalsResponseContentWithDefaults_whenParametersAreNull() {
        ListRentalsResponseContent response = new ListRentalsResponseContent();
        String defaultStatus = "ACTIVE";
        String defaultDateFrom = Instant.now().minus(30, ChronoUnit.DAYS).toString();
        String defaultDateTo = Instant.now().toString();

        when(rentalRepositoryPort.findByFilters(
            isNull(),
            eq(defaultStatus),
            anyString(),
            anyString(),
            isNull(),
            isNull()
        )).thenReturn(Flux.empty());

        when(rentalMapper.toListResponse(
            eq(Collections.emptyList()),
            eq(1),
            eq(20)
        )).thenReturn(response);

        StepVerifier.create(rentalService.list(null, null, null, null, null, null))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void list_shouldReturnError_whenRepositoryFails() {
        when(rentalRepositoryPort.findByFilters(any(), any(), any(), any(), any(), any())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalService.list(1, 10, "Test", "ACTIVE", "2023-01-01", "2023-12-31"))
                .expectError(RuntimeException.class)
                .verify();
    }
}
