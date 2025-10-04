package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long ownerId);

    ItemDto update(ItemDto itemDto, Long itemId, Long ownerId);

    void delete(Long itemId, Long ownerId);

    ItemDto findById(Long itemId);

    List<ItemDto> findAllByOwner(Long ownerId);

    List<ItemDto> search(String text);
}
