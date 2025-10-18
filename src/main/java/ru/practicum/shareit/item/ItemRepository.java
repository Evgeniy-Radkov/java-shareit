package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item create(Item item);

    Item update(Item item);

    void delete(Long itemId);

    Item findById(Long itemId);

    List<Item> findAllByOwner(Long ownerId);

    List<Item> search(String text);
}
