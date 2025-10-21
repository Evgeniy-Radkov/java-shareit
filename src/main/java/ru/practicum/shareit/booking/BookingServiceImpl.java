package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper mapper;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingCreateDto dto) {
        ensureUserExists(userId);
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("start/end не должны быть null");
        }
        if (!dto.getStart().isBefore(dto.getEnd())) {
            throw new ValidationException("start должен быть раньше end");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + dto.getItemId()));
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner() != null && item.getOwner().getId().equals(booker.getId())) {
            throw new ForbiddenException("Нельзя бронировать свою вещь");
        }

        boolean intersects = bookingRepository.existsByItemIdAndStatusAndStartLessThanAndEndGreaterThan(
                item.getId(), BookingStatus.APPROVED, dto.getEnd(), dto.getStart());
        if (intersects) {
            throw new ValidationException("На эти даты уже есть подтвержденная бронь");
        }

        Booking entity = mapper.toEntity(dto, item, booker);
        Booking saved = bookingRepository.save(entity);

        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new ForbiddenException("Подтверждать/отклонять может только владелец вещи"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена: " + bookingId));

        Long itemOwnerId = booking.getItem().getOwner().getId();
        if (!itemOwnerId.equals(ownerId)) {
            throw new ForbiddenException("Подтверждать/отклонять может только владелец вещи");
        }

        switch (booking.getStatus()) {
            case APPROVED, REJECTED, CANCELED ->
                    throw new ValidationException("Статус уже зафиксирован и не может быть изменен");
            case WAITING -> { }
        }

        if (approved) {
            boolean intersects = bookingRepository.existsByItemIdAndStatusAndStartLessThanAndEndGreaterThan(
                    booking.getItem().getId(),
                    BookingStatus.APPROVED,
                    booking.getEnd(),
                    booking.getStart()
            );
            if (intersects) {
                throw new ValidationException("Нельзя утвердить: даты пересекаются с другой подтвержденной бронью");
            }
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        Booking saved = bookingRepository.save(booking);
        return mapper.toDto(saved);
    }

    @Override
    public BookingDto findById(Long requesterId, Long bookingId) {
        ensureUserExists(requesterId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронь не найдена: " + bookingId));

        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!bookerId.equals(requesterId) && !ownerId.equals(requesterId)) {
            throw new ForbiddenException("Доступ разрешен только автору брони или владельцу вещи");
        }

        return mapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findByBooker(Long userId, BookingState state, int from, int size) {
        ensureUserExists(userId);
        if (from < 0 || size <= 0) {
            throw new ValidationException("from>=0 и size>0");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findAllByBookerIdOrderByStartDesc(userId, pageable);
            case CURRENT -> bookingRepository.findAllByBookerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                    userId, now, now, pageable);
            case PAST -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now, pageable);
            case FUTURE -> bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
            case WAITING -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(
                    userId, BookingStatus.REJECTED, pageable);
        };

        return page.getContent().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> findByOwner(Long ownerId, BookingState state, int from, int size) {
        ensureUserExists(ownerId);
        if (from < 0 || size <= 0) {
            throw new ValidationException("from>=0 и size>0");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        Page<Booking> page = switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId, pageable);
            case CURRENT -> bookingRepository.findAllByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                    ownerId, now, now, pageable);
            case PAST -> bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, pageable);
            case FUTURE -> bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now, pageable);
            case WAITING -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(
                    ownerId, BookingStatus.REJECTED, pageable);
        };

        return page.getContent().stream()
                .map(mapper::toDto)
                .toList();
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
    }
}
