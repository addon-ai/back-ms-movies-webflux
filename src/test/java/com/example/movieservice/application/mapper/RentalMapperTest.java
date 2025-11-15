
package com.example.movieservice.application.mapper;

import com.example.movieservice.application.dto.movie.CreateRentalRequestContent;
import com.example.movieservice.application.dto.movie.ListRentalsResponseContent;
import com.example.movieservice.application.dto.movie.RentalResponse;
import com.example.movieservice.application.dto.movie.UpdateRentalRequestContent;
import com.example.movieservice.domain.model.Rental;
import com.example.movieservice.infrastructure.adapters.output.persistence.entity.RentalDbo;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RentalMapperTest {

    private final RentalMapper rentalMapper = Mappers.getMapper(RentalMapper.class);

    @Test
    void toDbo_shouldMapRentalToRentalDbo() {
        Rental rental = Rental.builder().rentalId(UUID.randomUUID().toString()).build();
        RentalDbo rentalDbo = rentalMapper.toDbo(rental);
        assertEquals(rental.getRentalId(), rentalDbo.getId().toString());
    }

    @Test
    void toDomain_shouldMapRentalDboToRental() {
        RentalDbo rentalDbo = RentalDbo.builder().id(UUID.randomUUID()).build();
        Rental rental = rentalMapper.toDomain(rentalDbo);
        assertEquals(rentalDbo.getId().toString(), rental.getRentalId());
    }

    @Test
    void fromCreateRequest_shouldMapCreateRentalRequestContentToRental() {
        CreateRentalRequestContent request = CreateRentalRequestContent.builder()
                .movieId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .rentalDays(BigDecimal.ONE)
                .build();
        Rental rental = rentalMapper.fromCreateRequest(request);
        assertEquals(request.getMovieId(), rental.getMovieId());
        assertEquals(request.getUserId(), rental.getUserId());
        assertNotNull(rental.getCreatedAt());
        assertNotNull(rental.getUpdatedAt());
        assertEquals("ACTIVE", rental.getStatus());
    }

    @Test
    void updateEntityFromRequest_shouldUpdateRentalFromUpdateRentalRequestContent() {
        UpdateRentalRequestContent request = UpdateRentalRequestContent.builder().returnDate(Instant.now().toString()).build();
        Rental rental = new Rental();
        rentalMapper.updateEntityFromRequest(request, rental);
        assertEquals(request.getReturnDate(), rental.getReturnDate());
    }

    @Test
    void toDtoList_shouldMapListOfRentalsToListOfRentalResponses() {
        Rental rental = Rental.builder().build();
        List<RentalResponse> rentalResponses = rentalMapper.toDtoList(Collections.singletonList(rental));
        assertEquals(1, rentalResponses.size());
    }

    @Test
    void toListResponse_shouldCreatePaginatedResponse() {
        Rental rental = Rental.builder().build();
        List<Rental> rentals = Collections.singletonList(rental);
        ListRentalsResponseContent response = rentalMapper.toListResponse(rentals, 1, 10, 1);
        assertEquals(1, response.getRentals().size());
        assertEquals(BigDecimal.valueOf(1), response.getPage());
        assertEquals(BigDecimal.valueOf(10), response.getSize());
        assertEquals(BigDecimal.valueOf(1), response.getTotal());
        assertEquals(BigDecimal.valueOf(1), response.getTotalPages());
    }
}
