package ru.practicum.shareit.request;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemRequestShortMapper itemRequestShortMapper;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        ItemRequest toSave = itemRequestMapper.toEntity(dto, requester, LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(toSave);

        return itemRequestMapper.toDto(saved, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь не найден: " + userId));

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequester_IdOrderByCreatedDesc(requester.getId());

        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRepository.findAllByRequest_IdIn(requestIds);

        Map<Long, List<ItemRequestShortDto>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(itemRequestShortMapper::toShortDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> itemRequestMapper.toDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден: " + userId));

        int page = from / size;
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created"));

        Page<ItemRequest> pageReq = itemRequestRepository.findAllByRequester_IdNot(userId, pr);
        List<ItemRequest> requests = pageReq.getContent();
        if (requests.isEmpty()) return Collections.emptyList();

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        List<Item> items = itemRepository.findAllByRequest_IdIn(requestIds);

        Map<Long, List<ItemRequestShortDto>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(it -> it.getRequest().getId(),
                        Collectors.mapping(itemRequestShortMapper::toShortDto, Collectors.toList())));

        return requests.stream()
                .map(r -> itemRequestMapper.toDto(
                        r, itemsByRequest.getOrDefault(r.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден: " + userId));

        ItemRequest req = itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Запрос не найден: " + requestId));

        List<Item> items = itemRepository.findAllByRequest_Id(requestId);
        List<ItemRequestShortDto> shortDtos = items.stream()
                .map(itemRequestShortMapper::toShortDto)
                .toList();

        return itemRequestMapper.toDto(req, shortDtos);
    }
}
