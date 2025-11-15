
package com.example.movieservice.infrastructure.adapters.output.persistence.adapter;

import com.example.movieservice.application.mapper.RentalMapper;
import com.example.movieservice.domain.model.Rental;
import com.example.movieservice.infrastructure.adapters.output.persistence.entity.RentalDbo;
import com.example.movieservice.infrastructure.adapters.output.persistence.repository.JpaRentalRepository;
import com.example.movieservice.infrastructure.config.exceptions.InternalServerErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalRepositoryAdapterTest {

    @Mock
    private JpaRentalRepository jpaRentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalRepositoryAdapter rentalRepositoryAdapter;

    private Rental rental;
    private RentalDbo rentalDbo;
    private String rentalId;

    @BeforeEach
    void setUp() {
        rentalId = UUID.randomUUID().toString();
        rental = Rental.builder().rentalId(rentalId).build();
        rentalDbo = RentalDbo.builder().id(UUID.fromString(rentalId)).build();
    }

    @Test
    void save_shouldReturnRental_whenSuccessful() {
        when(rentalMapper.toDbo(any(Rental.class))).thenReturn(rentalDbo);
        when(jpaRentalRepository.save(any(RentalDbo.class))).thenReturn(Mono.just(rentalDbo));
        when(rentalMapper.toDomain(any(RentalDbo.class))).thenReturn(rental);

        StepVerifier.create(rentalRepositoryAdapter.save(rental))
                .expectNext(rental)
                .verifyComplete();

        verify(rentalMapper).toDbo(rental);
        verify(jpaRentalRepository).save(rentalDbo);
        verify(rentalMapper).toDomain(rentalDbo);
    }

    @Test
    void save_shouldPropagateDuplicateKeyException_whenThrown() {
        when(rentalMapper.toDbo(any(Rental.class))).thenReturn(rentalDbo);
        when(jpaRentalRepository.save(any(RentalDbo.class))).thenReturn(Mono.error(new DuplicateKeyException("Duplicate key")));

        StepVerifier.create(rentalRepositoryAdapter.save(rental))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void save_shouldPropagateDataIntegrityViolationException_whenThrown() {
        when(rentalMapper.toDbo(any(Rental.class))).thenReturn(rentalDbo);
        when(jpaRentalRepository.save(any(RentalDbo.class))).thenReturn(Mono.error(new DataIntegrityViolationException("Data integrity violation")));

        StepVerifier.create(rentalRepositoryAdapter.save(rental))
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    @Test
    void save_shouldMapToInternalServerError_whenOtherErrorOccurs() {
        when(rentalMapper.toDbo(any(Rental.class))).thenReturn(rentalDbo);
        when(jpaRentalRepository.save(any(RentalDbo.class))).thenReturn(Mono.error(new RuntimeException("Some other error")));

        StepVerifier.create(rentalRepositoryAdapter.save(rental))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findById_shouldReturnRental_whenFound() {
        when(jpaRentalRepository.findById(any(UUID.class))).thenReturn(Mono.just(rentalDbo));
        when(rentalMapper.toDomain(any(RentalDbo.class))).thenReturn(rental);

        StepVerifier.create(rentalRepositoryAdapter.findById(rentalId))
                .expectNext(rental)
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(jpaRentalRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(rentalRepositoryAdapter.findById(rentalId))
                .verifyComplete();
    }

    @Test
    void findById_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.findById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.findById(rentalId))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findAll_shouldReturnRentals_whenSuccessful() {
        when(jpaRentalRepository.findAll()).thenReturn(Flux.just(rentalDbo));
        when(rentalMapper.toDomain(any(RentalDbo.class))).thenReturn(rental);

        StepVerifier.create(rentalRepositoryAdapter.findAll())
                .expectNext(rental)
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnEmptyFlux_whenNoRentals() {
        when(jpaRentalRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(rentalRepositoryAdapter.findAll())
                .verifyComplete();
    }

    @Test
    void findAll_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.findAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.findAll())
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void deleteById_shouldComplete_whenSuccessful() {
        when(jpaRentalRepository.deleteById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(rentalRepositoryAdapter.deleteById(rentalId))
                .verifyComplete();
    }

    @Test
    void deleteById_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.deleteById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.deleteById(rentalId))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(jpaRentalRepository.existsById(any(UUID.class))).thenReturn(Mono.just(true));

        StepVerifier.create(rentalRepositoryAdapter.existsById(rentalId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(jpaRentalRepository.existsById(any(UUID.class))).thenReturn(Mono.just(false));

        StepVerifier.create(rentalRepositoryAdapter.existsById(rentalId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsById_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.existsById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.existsById(rentalId))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findBySearchTerm_shouldReturnRentals_whenSuccessful() {
        when(jpaRentalRepository.findBySearchTerm(anyString(), anyLong(), anyLong())).thenReturn(Flux.just(rentalDbo));
        when(rentalMapper.toDomain(any(RentalDbo.class))).thenReturn(rental);

        StepVerifier.create(rentalRepositoryAdapter.findBySearchTerm("Test", 1, 10))
                .expectNext(rental)
                .verifyComplete();
    }

    @Test
    void findBySearchTerm_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.findBySearchTerm(anyString(), anyLong(), anyLong())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.findBySearchTerm("Test", 1, 10))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findByFilters_shouldReturnRentals_whenSuccessful() {
        when(jpaRentalRepository.findByFilters(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong())).thenReturn(Flux.just(rentalDbo));
        when(rentalMapper.toDomain(any(RentalDbo.class))).thenReturn(rental);

        StepVerifier.create(rentalRepositoryAdapter.findByFilters("Test", "ACTIVE", "2023-01-01", "2023-12-31", 1, 10))
                .expectNext(rental)
                .verifyComplete();
    }

    @Test
    void findByFilters_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.findByFilters(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.findByFilters("Test", "ACTIVE", "2023-01-01", "2023-12-31", 1, 10))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void countBySearchTerm_shouldReturnCount_whenSuccessful() {
        when(jpaRentalRepository.countBySearchTerm(anyString())).thenReturn(Mono.just(1L));

        StepVerifier.create(rentalRepositoryAdapter.countBySearchTerm("Test"))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void countBySearchTerm_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.countBySearchTerm(anyString())).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.countBySearchTerm("Test"))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findAllPaged_shouldReturnRentals_whenSuccessful() {
        when(jpaRentalRepository.findAllPaged(anyLong(), anyLong())).thenReturn(Flux.just(rentalDbo));
        when(rentalMapper.toDomain(any(RentalDbo.class))).thenReturn(rental);

        StepVerifier.create(rentalRepositoryAdapter.findAllPaged(1, 10))
                .expectNext(rental)
                .verifyComplete();
    }

    @Test
    void findAllPaged_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.findAllPaged(anyLong(), anyLong())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.findAllPaged(1, 10))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void countAll_shouldReturnCount_whenSuccessful() {
        when(jpaRentalRepository.countAll()).thenReturn(Mono.just(10L));

        StepVerifier.create(rentalRepositoryAdapter.countAll())
                .expectNext(10L)
                .verifyComplete();
    }

    @Test
    void countAll_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaRentalRepository.countAll()).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(rentalRepositoryAdapter.countAll())
                .expectError(InternalServerErrorException.class)
                .verify();
    }
}
