package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto create(ItemDto dto, Long ownerId) {
        User foundOwner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + ownerId));

        Item itemToSave = itemMapper.toItem(dto);
        itemToSave.setOwner(foundOwner);
        Item saved = itemRepository.save(itemToSave);
        return itemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(ItemDto dto, Long itemId, Long ownerId) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }

        dto.setId(null);

        itemMapper.updateItemFromDto(dto, existing);

        Item updated = itemRepository.save(existing);
        return itemMapper.toItemDto(updated);
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Удалять вещь может только владелец");
        }

        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));

        ItemDto dto = itemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId)
                .stream()
                .map(commentMapper::toDto)
                .toList();
        dto.setComments(comments);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository.findTopByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                    itemId, BookingStatus.APPROVED, now
            ).ifPresent(b -> dto.setLastBooking(toShort(b)));

            bookingRepository.findTopByItemIdAndStatusAndStartAfterOrderByStartAsc(
                    itemId, BookingStatus.APPROVED, now
            ).ifPresent(b -> dto.setNextBooking(toShort(b)));
        }

        return dto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> findAllByOwner(Long ownerId, int from, int size) {
        if (from < 0 || size <= 0) throw new ValidationException("from>=0 и size>0");

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(ownerId, pageable);

        LocalDateTime now = LocalDateTime.now();

        return items.stream().map(item -> {
            ItemDto dto = itemMapper.toItemDto(item);

            List<CommentDto> comments = commentRepository
                    .findAllByItemIdOrderByCreatedDesc(item.getId())
                    .stream()
                    .map(commentMapper::toDto)
                    .toList();
            dto.setComments(comments);

            bookingRepository.findTopByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                    item.getId(), BookingStatus.APPROVED, now
            ).ifPresent(b -> dto.setLastBooking(toShort(b)));

            bookingRepository.findTopByItemIdAndStatusAndStartAfterOrderByStartAsc(
                    item.getId(), BookingStatus.APPROVED, now
            ).ifPresent(b -> dto.setNextBooking(toShort(b)));

            return dto;
        }).toList();
    }


    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return  itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена: " + itemId));
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        LocalDateTime now = LocalDateTime.now();
        boolean canComment = bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, BookingStatus.APPROVED, now);
        if (!canComment) {
            throw new ValidationException("Отзыв доступен только после завершённой аренды");
        }

        Comment entity = commentMapper.toEntity(dto, item, author, now);
        Comment saved = commentRepository.save(entity);
        return commentMapper.toDto(saved);
    }

    private BookingShortDto toShort(Booking booking) {
        if (booking == null) return null;
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        return dto;
    }
}
