package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Item item, Long ownerId);

    Item update(Item item, Long itemId, Long ownerId);

    void delete(Long itemId, Long ownerId);

    Item findById(Long itemId);

    List<Item> findAll();

    List<Item> findAllByOwner(Long ownerId);

    List<Item> search(String text);
}
