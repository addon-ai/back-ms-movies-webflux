
package com.example.movieservice.infrastructure.adapters.output.persistence.adapter;

import com.example.movieservice.application.mapper.MovieMapper;
import com.example.movieservice.domain.model.Movie;
import com.example.movieservice.infrastructure.adapters.output.persistence.entity.MovieDbo;
import com.example.movieservice.infrastructure.adapters.output.persistence.repository.JpaMovieRepository;
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
class MovieRepositoryAdapterTest {

    @Mock
    private JpaMovieRepository jpaMovieRepository;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieRepositoryAdapter movieRepositoryAdapter;

    private Movie movie;
    private MovieDbo movieDbo;
    private String movieId;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID().toString();
        movie = Movie.builder()
                .movieId(movieId)
                .title("Test Movie")
                .build();

        movieDbo = MovieDbo.builder()
                .id(UUID.fromString(movieId))
                .title("Test Movie")
                .build();
    }

    @Test
    void save_shouldReturnMovie_whenSuccessful() {
        when(movieMapper.toDbo(any(Movie.class))).thenReturn(movieDbo);
        when(jpaMovieRepository.save(any(MovieDbo.class))).thenReturn(Mono.just(movieDbo));
        when(movieMapper.toDomain(any(MovieDbo.class))).thenReturn(movie);

        StepVerifier.create(movieRepositoryAdapter.save(movie))
                .expectNext(movie)
                .verifyComplete();

        verify(movieMapper).toDbo(movie);
        verify(jpaMovieRepository).save(movieDbo);
        verify(movieMapper).toDomain(movieDbo);
    }

    @Test
    void save_shouldPropagateDuplicateKeyException_whenThrown() {
        when(movieMapper.toDbo(any(Movie.class))).thenReturn(movieDbo);
        when(jpaMovieRepository.save(any(MovieDbo.class))).thenReturn(Mono.error(new DuplicateKeyException("Duplicate key")));

        StepVerifier.create(movieRepositoryAdapter.save(movie))
                .expectError(DuplicateKeyException.class)
                .verify();
    }

    @Test
    void save_shouldPropagateDataIntegrityViolationException_whenThrown() {
        when(movieMapper.toDbo(any(Movie.class))).thenReturn(movieDbo);
        when(jpaMovieRepository.save(any(MovieDbo.class))).thenReturn(Mono.error(new DataIntegrityViolationException("Data integrity violation")));

        StepVerifier.create(movieRepositoryAdapter.save(movie))
                .expectError(DataIntegrityViolationException.class)
                .verify();
    }

    @Test
    void save_shouldMapToInternalServerError_whenOtherErrorOccurs() {
        when(movieMapper.toDbo(any(Movie.class))).thenReturn(movieDbo);
        when(jpaMovieRepository.save(any(MovieDbo.class))).thenReturn(Mono.error(new RuntimeException("Some other error")));

        StepVerifier.create(movieRepositoryAdapter.save(movie))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findById_shouldReturnMovie_whenFound() {
        when(jpaMovieRepository.findById(any(UUID.class))).thenReturn(Mono.just(movieDbo));
        when(movieMapper.toDomain(any(MovieDbo.class))).thenReturn(movie);

        StepVerifier.create(movieRepositoryAdapter.findById(movieId))
                .expectNext(movie)
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(jpaMovieRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(movieRepositoryAdapter.findById(movieId))
                .verifyComplete();
    }

    @Test
    void findById_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.findById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.findById(movieId))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findAll_shouldReturnMovies_whenSuccessful() {
        when(jpaMovieRepository.findAll()).thenReturn(Flux.just(movieDbo));
        when(movieMapper.toDomain(any(MovieDbo.class))).thenReturn(movie);

        StepVerifier.create(movieRepositoryAdapter.findAll())
                .expectNext(movie)
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnEmptyFlux_whenNoMovies() {
        when(jpaMovieRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(movieRepositoryAdapter.findAll())
                .verifyComplete();
    }

    @Test
    void findAll_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.findAll()).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.findAll())
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void deleteById_shouldComplete_whenSuccessful() {
        when(jpaMovieRepository.deleteById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(movieRepositoryAdapter.deleteById(movieId))
                .verifyComplete();
    }

    @Test
    void deleteById_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.deleteById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.deleteById(movieId))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(jpaMovieRepository.existsById(any(UUID.class))).thenReturn(Mono.just(true));

        StepVerifier.create(movieRepositoryAdapter.existsById(movieId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(jpaMovieRepository.existsById(any(UUID.class))).thenReturn(Mono.just(false));

        StepVerifier.create(movieRepositoryAdapter.existsById(movieId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void existsById_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.existsById(any(UUID.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.existsById(movieId))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findBySearchTerm_shouldReturnMovies_whenSuccessful() {
        when(jpaMovieRepository.findBySearchTerm(anyString(), anyLong(), anyLong())).thenReturn(Flux.just(movieDbo));
        when(movieMapper.toDomain(any(MovieDbo.class))).thenReturn(movie);

        StepVerifier.create(movieRepositoryAdapter.findBySearchTerm("Test", 1, 10))
                .expectNext(movie)
                .verifyComplete();
    }

    @Test
    void findBySearchTerm_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.findBySearchTerm(anyString(), anyLong(), anyLong())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.findBySearchTerm("Test", 1, 10))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findByFilters_shouldReturnMovies_whenSuccessful() {
        when(jpaMovieRepository.findByFilters(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong())).thenReturn(Flux.just(movieDbo));
        when(movieMapper.toDomain(any(MovieDbo.class))).thenReturn(movie);

        StepVerifier.create(movieRepositoryAdapter.findByFilters("Test", "ACTIVE", "2023-01-01", "2023-12-31", 1, 10))
                .expectNext(movie)
                .verifyComplete();
    }

    @Test
    void findByFilters_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.findByFilters(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.findByFilters("Test", "ACTIVE", "2023-01-01", "2023-12-31", 1, 10))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void countBySearchTerm_shouldReturnCount_whenSuccessful() {
        when(jpaMovieRepository.countBySearchTerm(anyString())).thenReturn(Mono.just(1L));

        StepVerifier.create(movieRepositoryAdapter.countBySearchTerm("Test"))
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void countBySearchTerm_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.countBySearchTerm(anyString())).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.countBySearchTerm("Test"))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void findAllPaged_shouldReturnMovies_whenSuccessful() {
        when(jpaMovieRepository.findAllPaged(anyLong(), anyLong())).thenReturn(Flux.just(movieDbo));
        when(movieMapper.toDomain(any(MovieDbo.class))).thenReturn(movie);

        StepVerifier.create(movieRepositoryAdapter.findAllPaged(1, 10))
                .expectNext(movie)
                .verifyComplete();
    }

    @Test
    void findAllPaged_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.findAllPaged(anyLong(), anyLong())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.findAllPaged(1, 10))
                .expectError(InternalServerErrorException.class)
                .verify();
    }

    @Test
    void countAll_shouldReturnCount_whenSuccessful() {
        when(jpaMovieRepository.countAll()).thenReturn(Mono.just(10L));

        StepVerifier.create(movieRepositoryAdapter.countAll())
                .expectNext(10L)
                .verifyComplete();
    }

    @Test
    void countAll_shouldMapToInternalServerError_whenErrorOccurs() {
        when(jpaMovieRepository.countAll()).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieRepositoryAdapter.countAll())
                .expectError(InternalServerErrorException.class)
                .verify();
    }
}
