package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemDto create(ItemDto dto, Long ownerId) {
        if (dto == null) {
            throw new ValidationException("Вещь не может быть null");
        }

        User owner = userRepository.findById(ownerId);

        Item item = itemMapper.toItem(dto);
        item.setOwner(owner);

        Item created = itemRepository.create(item);
        return itemMapper.toItemDto(created);
    }

    @Override
    public ItemDto update(ItemDto dto, Long itemId, Long ownerId) {
        Item existing = itemRepository.findById(itemId);

        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только владелец");
        }

        itemMapper.updateItemFromDto(dto, existing);

        Item updated = itemRepository.update(existing);
        return itemMapper.toItemDto(updated);
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
    public ItemDto findById(Long itemId) {
        return itemMapper.toItemDto(itemRepository.findById(itemId));
    }

    @Override
    public List<ItemDto> findAllByOwner(Long ownerId) {
        return itemRepository.findAllByOwner(ownerId).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }
}
