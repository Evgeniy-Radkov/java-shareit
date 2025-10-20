package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(ItemDto itemDto, Long itemId, Long ownerId);

    void delete(Long itemId, Long ownerId);

    ItemDto findById(Long itemId, Long userId);

    List<ItemDto> findAllByOwner(Long ownerId, int from, int size);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentCreateDto dto);
}
