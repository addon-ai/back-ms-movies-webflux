
package com.example.movieservice.application.mapper;

import com.example.movieservice.application.dto.movie.CreateMovieRequestContent;
import com.example.movieservice.application.dto.movie.ListMoviesResponseContent;
import com.example.movieservice.application.dto.movie.MovieResponse;
import com.example.movieservice.application.dto.movie.UpdateMovieRequestContent;
import com.example.movieservice.domain.model.Movie;
import com.example.movieservice.infrastructure.adapters.output.persistence.entity.MovieDbo;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MovieMapperTest {

    private final MovieMapper movieMapper = Mappers.getMapper(MovieMapper.class);

    @Test
    void toDbo_shouldMapMovieToMovieDbo() {
        Movie movie = Movie.builder().movieId(UUID.randomUUID().toString()).title("Test Movie").build();
        MovieDbo movieDbo = movieMapper.toDbo(movie);
        assertEquals(movie.getMovieId(), movieDbo.getId().toString());
        assertEquals(movie.getTitle(), movieDbo.getTitle());
    }

    @Test
    void toDomain_shouldMapMovieDboToMovie() {
        MovieDbo movieDbo = MovieDbo.builder().id(UUID.randomUUID()).title("Test Movie").build();
        Movie movie = movieMapper.toDomain(movieDbo);
        assertEquals(movieDbo.getId().toString(), movie.getMovieId());
        assertEquals(movieDbo.getTitle(), movie.getTitle());
    }

    @Test
    void fromCreateRequest_shouldMapCreateMovieRequestContentToMovie() {
        CreateMovieRequestContent request = CreateMovieRequestContent.builder().title("Test Movie").build();
        Movie movie = movieMapper.fromCreateRequest(request);
        assertEquals(request.getTitle(), movie.getTitle());
        assertNotNull(movie.getCreatedAt());
        assertNotNull(movie.getUpdatedAt());
        assertEquals("ACTIVE", movie.getStatus());
    }

    @Test
    void updateEntityFromRequest_shouldUpdateMovieFromUpdateMovieRequestContent() {
        UpdateMovieRequestContent request = UpdateMovieRequestContent.builder().title("Updated Movie").build();
        Movie movie = Movie.builder().title("Original Movie").build();
        movieMapper.updateEntityFromRequest(request, movie);
        assertEquals(request.getTitle(), movie.getTitle());
    }

    @Test
    void toDtoList_shouldMapListOfMoviesToListOfMovieResponses() {
        Movie movie = Movie.builder().title("Test Movie").build();
        List<MovieResponse> movieResponses = movieMapper.toDtoList(Collections.singletonList(movie));
        assertEquals(1, movieResponses.size());
        assertEquals(movie.getTitle(), movieResponses.get(0).getTitle());
    }

    @Test
    void toListResponse_shouldCreatePaginatedResponse() {
        Movie movie = Movie.builder().title("Test Movie").build();
        List<Movie> movies = Collections.singletonList(movie);
        ListMoviesResponseContent response = movieMapper.toListResponse(movies, 1, 10, 1);
        assertEquals(1, response.getMovies().size());
        assertEquals(BigDecimal.valueOf(1), response.getPage());
        assertEquals(BigDecimal.valueOf(10), response.getSize());
        assertEquals(BigDecimal.valueOf(1), response.getTotal());
        assertEquals(BigDecimal.valueOf(1), response.getTotalPages());
    }
}
