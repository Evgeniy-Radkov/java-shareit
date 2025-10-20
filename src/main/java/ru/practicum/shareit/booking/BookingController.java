package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService service;

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @Valid @RequestBody BookingCreateDto dto) {
        return service.create(userId, dto);
    }

    @PatchMapping("/{id}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                              @PathVariable Long id,
                              @RequestParam("approved") boolean approved) {
        return service.approve(ownerId, id, approved);
    }

    @GetMapping("/{id}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id") Long requesterId,
                               @PathVariable Long id) {
        return service.findById(requesterId, id);
    }

    @GetMapping
    public List<BookingDto> findByBooker(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "ALL") BookingState state,
                                         @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(defaultValue = "20") @Positive int size) {
        return service.findByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> findByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                        @RequestParam(defaultValue = "ALL") BookingState state,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "20") @Positive int size) {
        return service.findByOwner(ownerId, state, from, size);
    }
}
