
package com.example.movieservice.application.service;

import com.example.movieservice.application.dto.movie.*;
import com.example.movieservice.application.mapper.MovieMapper;
import com.example.movieservice.domain.model.Movie;
import com.example.movieservice.domain.ports.output.MovieRepositoryPort;
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
class MovieServiceTest {

    @Mock
    private MovieRepositoryPort movieRepositoryPort;

    @Mock
    private MovieMapper movieMapper;

    @InjectMocks
    private MovieService movieService;

    private Movie movie;
    private String movieId;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID().toString();
        movie = Movie.builder()
                .movieId(movieId)
                .title("Test Movie")
                .status("ACTIVE")
                .build();
    }

    @Test
    void create_shouldReturnCreateMovieResponseContent_whenSuccessful() {
        CreateMovieRequestContent request = new CreateMovieRequestContent();
        CreateMovieResponseContent response = new CreateMovieResponseContent();

        when(movieMapper.fromCreateRequest(any(CreateMovieRequestContent.class))).thenReturn(movie);
        when(movieRepositoryPort.save(any(Movie.class))).thenReturn(Mono.just(movie));
        when(movieMapper.toCreateResponse(any(Movie.class))).thenReturn(response);

        StepVerifier.create(movieService.create(request))
                .expectNext(response)
                .verifyComplete();

        verify(movieMapper).fromCreateRequest(request);
        verify(movieRepositoryPort).save(movie);
        verify(movieMapper).toCreateResponse(movie);
    }

    @Test
    void create_shouldReturnError_whenRepositoryFails() {
        CreateMovieRequestContent request = new CreateMovieRequestContent();
        when(movieMapper.fromCreateRequest(any(CreateMovieRequestContent.class))).thenReturn(movie);
        when(movieRepositoryPort.save(any(Movie.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieService.create(request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void get_shouldReturnGetMovieResponseContent_whenMovieFound() {
        GetMovieResponseContent response = new GetMovieResponseContent();
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.just(movie));
        when(movieMapper.toGetResponse(any(Movie.class))).thenReturn(response);

        StepVerifier.create(movieService.get(movieId))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void get_shouldThrowNotFoundException_whenMovieNotFound() {
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.empty());

        StepVerifier.create(movieService.get(movieId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void get_shouldReturnError_whenRepositoryFails() {
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieService.get(movieId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void update_shouldReturnUpdateMovieResponseContent_whenSuccessful() {
        UpdateMovieRequestContent request = new UpdateMovieRequestContent();
        UpdateMovieResponseContent response = new UpdateMovieResponseContent();

        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.just(movie));
        doNothing().when(movieMapper).updateEntityFromRequest(any(UpdateMovieRequestContent.class), any(Movie.class));
        when(movieRepositoryPort.save(any(Movie.class))).thenReturn(Mono.just(movie));
        when(movieMapper.toUpdateResponse(any(Movie.class))).thenReturn(response);

        StepVerifier.create(movieService.update(movieId, request))
                .expectNext(response)
                .verifyComplete();

        verify(movieMapper).updateEntityFromRequest(request, movie);
        verify(movieRepositoryPort).save(movie);
        verify(movieMapper).toUpdateResponse(movie);
    }

    @Test
    void update_shouldThrowNotFoundException_whenMovieNotFound() {
        UpdateMovieRequestContent request = new UpdateMovieRequestContent();
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.empty());

        StepVerifier.create(movieService.update(movieId, request))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void update_shouldReturnError_whenRepositoryFails() {
        UpdateMovieRequestContent request = new UpdateMovieRequestContent();
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.just(movie));
        doNothing().when(movieMapper).updateEntityFromRequest(any(UpdateMovieRequestContent.class), any(Movie.class));
        when(movieRepositoryPort.save(any(Movie.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieService.update(movieId, request))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void delete_shouldReturnDeleteMovieResponseContent_whenSuccessful() {
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.just(movie));
        when(movieRepositoryPort.save(any(Movie.class))).thenReturn(Mono.just(movie));

        StepVerifier.create(movieService.delete(movieId))
                .expectNextMatches(response -> response.getDeleted() && "Movie deleted successfully".equals(response.getMessage()))
                .verifyComplete();

        verify(movieRepositoryPort).save(movie);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenMovieNotFound() {
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.empty());

        StepVerifier.create(movieService.delete(movieId))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void delete_shouldReturnError_whenRepositoryFails() {
        when(movieRepositoryPort.findById(movieId)).thenReturn(Mono.just(movie));
        when(movieRepositoryPort.save(any(Movie.class))).thenReturn(Mono.error(new RuntimeException("Database error")));

        StepVerifier.create(movieService.delete(movieId))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void list_shouldReturnListMoviesResponseContent_whenSuccessful() {
        ListMoviesResponseContent response = new ListMoviesResponseContent();
        when(movieRepositoryPort.findByFilters(any(), any(), any(), any(), any(), any())).thenReturn(Flux.just(movie));
        when(movieMapper.toListResponse(any(), anyInt(), anyInt())).thenReturn(response);

        StepVerifier.create(movieService.list(1, 10, "Test", "ACTIVE", "2023-01-01", "2023-12-31"))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void list_shouldReturnListMoviesResponseContentWithDefaults_whenParametersAreNull() {
        ListMoviesResponseContent response = new ListMoviesResponseContent();
        String defaultStatus = "ACTIVE";
        String defaultDateFrom = Instant.now().minus(30, ChronoUnit.DAYS).toString();
        String defaultDateTo = Instant.now().toString();

        when(movieRepositoryPort.findByFilters(
            isNull(),
            eq(defaultStatus),
            anyString(),
            anyString(),
            isNull(),
            isNull()
        )).thenReturn(Flux.empty());

        when(movieMapper.toListResponse(
            eq(Collections.emptyList()),
            eq(1),
            eq(20)
        )).thenReturn(response);

        StepVerifier.create(movieService.list(null, null, null, null, null, null))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void list_shouldReturnError_whenRepositoryFails() {
        when(movieRepositoryPort.findByFilters(any(), any(), any(), any(), any(), any())).thenReturn(Flux.error(new RuntimeException("Database error")));

        StepVerifier.create(movieService.list(1, 10, "Test", "ACTIVE", "2023-01-01", "2023-12-31"))
                .expectError(RuntimeException.class)
                .verify();
    }
}
