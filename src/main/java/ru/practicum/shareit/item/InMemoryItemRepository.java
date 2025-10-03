package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    private long nextId = 1;

    @Override
    public Item create(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (item.getId() == null) {
            throw new ValidationException("Id не может быть null");
        }
        if (items.containsKey(item.getId())) {
            items.replace(item.getId(), item);
            return item;
        } else {
            throw new NotFoundException("Вещь не найдена");
        }
    }

    @Override
    public void delete(Long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Вещь не найдена: " + id);
        }
        items.remove(id);
    }

    @Override
    public Item findById(Long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Вещь не найдена: " + id);
        }
        return items.get(id);
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.trim().isBlank()) {
            return new ArrayList<>();
        }
        String q = text.toLowerCase().trim();
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getAvailable() == true && (
                    (item.getName() != null && item.getName().toLowerCase().contains(q))
                            || (item.getDescription() != null && item.getDescription().toLowerCase().contains(q))
            )) {
                result.add(item);
            }
        }
        return result;
    }
}
