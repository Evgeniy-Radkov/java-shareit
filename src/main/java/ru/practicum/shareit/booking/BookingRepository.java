package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId, Pageable p);

    Page<Booking> findAllByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2, Pageable p);

    Page<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable p);

    Page<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now, Pageable p);

    Page<Booking> findAllByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable p);

    Page<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable p);

    Page<Booking> findAllByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId, LocalDateTime now1, LocalDateTime now2, Pageable p);

    Page<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime now, Pageable p);

    Page<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime now, Pageable p);

    Page<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status, Pageable p);

    Optional<Booking> findTopByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findTopByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByItemIdAndStatusAndStartLessThanAndEndGreaterThan(
            Long itemId, BookingStatus status, LocalDateTime newEnd, LocalDateTime newStart);
}
