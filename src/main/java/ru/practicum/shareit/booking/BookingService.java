package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingCreateDto dto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto findById(Long requesterId, Long bookingId);

    List<BookingDto> findByBooker(Long userId, BookingState state, int from, int size);

    List<BookingDto> findByOwner(Long ownerId, BookingState state, int from, int size);
}