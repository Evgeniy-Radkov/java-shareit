package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

import static ru.practicum.shareit.constant.Headers.X_SHARER_USER_ID;


@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                 @RequestBody ItemRequestCreateDto dto) {
        return itemRequestService.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        return itemRequestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                       @RequestParam(name = "from", defaultValue = "0") int from,
                                       @RequestParam(name = "size", defaultValue = "10") int size) {
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@RequestHeader(X_SHARER_USER_ID) Long userId,
                                  @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}
