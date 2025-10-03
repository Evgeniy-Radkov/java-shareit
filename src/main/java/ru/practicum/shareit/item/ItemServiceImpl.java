package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public Item create(Item item, Long ownerId) {
        validateItem(item);
        User owner = userService.findById(ownerId);
        item.setOwner(owner);
        return itemRepository.create(item);
    }

    @Override
    public Item update(Item patch, Long itemId, Long ownerId) {
        Item existing = itemRepository.findById(itemId);

        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }

        // PATCH-merge
        if (patch.getName() != null && !patch.getName().isBlank()) {
            existing.setName(patch.getName().trim());
        }
        if (patch.getDescription() != null && !patch.getDescription().isBlank()) {
            existing.setDescription(patch.getDescription().trim());
        }
        if (patch.getAvailable() != null) {
            existing.setAvailable(patch.getAvailable());
        }

        validateItem(existing);
        return itemRepository.update(existing);
    }

    @Override
    public void delete(Long itemId, Long ownerId) {
        Item existing = itemRepository.findById(itemId);
        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Удалять вещь может только владелец");
        }
        itemRepository.delete(itemId);
    }

    @Override
    public Item findById(Long itemId) {
        return itemRepository.findById(itemId);
    }

    @Override
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    @Override
    public List<Item> findAllByOwner(Long ownerId) {
        return itemRepository.findAll().stream()
                .filter(i -> i.getOwner() != null && i.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        return itemRepository.search(text);
    }

    private void validateItem(Item item) {
        if (item == null) {
            throw new ValidationException("Вещь не может быть null");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Статус доступности обязателен");
        }
    }
}
