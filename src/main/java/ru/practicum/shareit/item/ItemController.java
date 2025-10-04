package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static ru.practicum.shareit.constant.Headers.X_SHARER_USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader(X_SHARER_USER_ID) Long ownerId,
                              @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(X_SHARER_USER_ID) Long ownerId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.update(itemDto, itemId, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto findById(@PathVariable Long itemId) {
        return itemService.findById(itemId);
    }

    @GetMapping
    public List<ItemDto> findAllByOwner(@RequestHeader(X_SHARER_USER_ID) Long ownerId) {
        return itemService.findAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@RequestHeader(X_SHARER_USER_ID) Long ownerId,
                           @PathVariable Long itemId) {
        itemService.delete(itemId, ownerId);
    }
}
