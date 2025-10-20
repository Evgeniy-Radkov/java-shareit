package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
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
    public ItemDto findById(
            @RequestHeader(X_SHARER_USER_ID) Long userId,
            @PathVariable Long itemId) {
        return itemService.findById(itemId, userId);
    }


    @GetMapping
    public List<ItemDto> findAllByOwner(
            @RequestHeader(X_SHARER_USER_ID) Long ownerId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        return itemService.findAllByOwner(ownerId, from, size);
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

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentCreateDto dto) {
        return itemService.addComment(userId, itemId, dto);
    }
}
