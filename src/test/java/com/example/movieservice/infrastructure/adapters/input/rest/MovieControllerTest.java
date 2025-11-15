
package com.example.movieservice.infrastructure.adapters.input.rest;

import com.example.movieservice.application.dto.movie.*;
import com.example.movieservice.domain.ports.input.MovieUseCase;
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

@WebFluxTest(controllers = MovieController.class, excludeAutoConfiguration = {ReactiveSecurityAutoConfiguration.class})
@Import(GlobalExceptionHandler.class)
class MovieControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieUseCase movieUseCase;

    private String movieId;
    private String requestId;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID().toString();
        requestId = UUID.randomUUID().toString();
    }

    @Test
    void createMovie_shouldReturnCreated_whenSuccessful() {
        CreateMovieRequestContent request = CreateMovieRequestContent.builder()
                .title("Test Movie")
                .director("Test Director")
                .genre("Test Genre")
                .releaseYear(BigDecimal.valueOf(2022))
                .duration(BigDecimal.valueOf(120))
                .availableCopies(BigDecimal.valueOf(10))
                .rentalPrice(5.99)
                .build();
        CreateMovieResponseContent response = CreateMovieResponseContent.builder()
                .movieId(movieId)
                .build();
        when(movieUseCase.create(any(CreateMovieRequestContent.class))).thenReturn(Mono.just(response));

        webTestClient.post().uri("/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId)
                .body(Mono.just(request), CreateMovieRequestContent.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateMovieResponseContent.class)
                .value(res -> {
                    assert res.getMovieId().equals(movieId);
                });
    }

    @Test
    void getMovie_shouldReturnOk_whenMovieFound() {
        GetMovieResponseContent response = new GetMovieResponseContent();
        when(movieUseCase.get(movieId)).thenReturn(Mono.just(response));

        webTestClient.get().uri("/movies/{movieId}", movieId)
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getMovie_shouldReturnNotFound_whenMovieNotFound() {
        when(movieUseCase.get(movieId)).thenReturn(Mono.error(new NotFoundException("Movie not found")));

        webTestClient.get().uri("/movies/{movieId}", movieId)
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateMovie_shouldReturnOk_whenSuccessful() {
        UpdateMovieRequestContent request = UpdateMovieRequestContent.builder()
                .title("Updated Movie")
                .build();
        UpdateMovieResponseContent response = new UpdateMovieResponseContent();
        when(movieUseCase.update(any(String.class), any(UpdateMovieRequestContent.class))).thenReturn(Mono.just(response));

        webTestClient.put().uri("/movies/{movieId}", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId)
                .body(Mono.just(request), UpdateMovieRequestContent.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateMovie_shouldReturnNotFound_whenMovieNotFound() {
        UpdateMovieRequestContent request = UpdateMovieRequestContent.builder()
                .title("Updated Movie")
                .build();
        when(movieUseCase.update(any(String.class), any(UpdateMovieRequestContent.class))).thenReturn(Mono.error(new NotFoundException("Movie not found")));

        webTestClient.put().uri("/movies/{movieId}", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-ID", requestId)
                .body(Mono.just(request), UpdateMovieRequestContent.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteMovie_shouldReturnOk_whenSuccessful() {
        DeleteMovieResponseContent response = DeleteMovieResponseContent.builder().deleted(true).message("Movie deleted successfully").build();
        when(movieUseCase.delete(movieId)).thenReturn(Mono.just(response));

        webTestClient.delete().uri("/movies/{movieId}", movieId)
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deleteMovie_shouldReturnNotFound_whenMovieNotFound() {
        when(movieUseCase.delete(movieId)).thenReturn(Mono.error(new NotFoundException("Movie not found")));

        webTestClient.delete().uri("/movies/{movieId}", movieId)
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void listMovies_shouldReturnOk_whenSuccessful() {
        ListMoviesResponseContent response = new ListMoviesResponseContent();
        when(movieUseCase.list(any(), any(), any(), any(), any(), any())).thenReturn(Mono.just(response));

        webTestClient.get().uri("/movies")
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listMovies_shouldReturnBadRequest_whenDateFromIsAfterDateTo() {
        String dateFrom = Instant.now().toString();
        String dateTo = Instant.now().minusSeconds(100).toString();
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/movies")
                        .queryParam("dateFrom", dateFrom)
                        .queryParam("dateTo", dateTo)
                        .build())
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void listMovies_shouldReturnBadRequest_whenDateFormatIsInvalid() {
        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/movies")
                        .queryParam("dateFrom", "invalid-date")
                        .queryParam("dateTo", "invalid-date")
                        .build())
                .header("X-Request-ID", requestId)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
