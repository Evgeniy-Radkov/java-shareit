package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIT {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User requester;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Requester");
        user.setEmail("requester@example.com");
        requester = userRepository.save(user);
    }

    @Test
    void create_persistsWithCreated_ok() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Нужна дрель с ударным режимом");

        ItemRequestDto created = itemRequestService.create(requester.getId(), dto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна дрель с ударным режимом");
        assertThat(created.getCreated()).isNotNull();

        Optional<ItemRequest> fromDb = itemRequestRepository.findById(created.getId());
        assertThat(fromDb).isPresent();

        ItemRequest entity = fromDb.get();
        assertThat(entity.getDescription()).isEqualTo("Нужна дрель с ударным режимом");
        assertThat(entity.getRequester().getId()).isEqualTo(requester.getId());
        assertThat(entity.getCreated()).isNotNull();
    }

    @Test
    void getOwn_sortedByCreatedDesc_withAnswers_ok() throws Exception {
        ItemRequestCreateDto d1 = new ItemRequestCreateDto();
        d1.setDescription("Нужна дрель");
        ItemRequestDto r1 = itemRequestService.create(requester.getId(), d1);

        Thread.sleep(5);

        ItemRequestCreateDto d2 = new ItemRequestCreateDto();
        d2.setDescription("Нужна болгарка");
        ItemRequestDto r2 = itemRequestService.create(requester.getId(), d2);

        Thread.sleep(5);

        ItemRequestCreateDto d3 = new ItemRequestCreateDto();
        d3.setDescription("Нужен перфоратор");
        ItemRequestDto r3 = itemRequestService.create(requester.getId(), d3);

        List<ItemRequestDto> own = itemRequestService.getOwnRequests(requester.getId());

        assertThat(own).isNotNull();
        assertThat(own).hasSize(3);

        assertThat(own.get(0).getId()).isEqualTo(r3.getId());
        assertThat(own.get(1).getId()).isEqualTo(r2.getId());
        assertThat(own.get(2).getId()).isEqualTo(r1.getId());

        assertThat(own.get(0).getCreated()).isNotNull();
        assertThat(own.get(1).getCreated()).isNotNull();
        assertThat(own.get(2).getCreated()).isNotNull();
    }

    @Test
    void getAll_excludesOwn_withPagination_ok() throws Exception {
        User other1 = new User();
        other1.setName("Other1");
        other1.setEmail("other1@example.com");
        other1 = userRepository.save(other1);

        User other2 = new User();
        other2.setName("Other2");
        other2.setEmail("other2@example.com");
        other2 = userRepository.save(other2);

        ItemRequestCreateDto own1 = new ItemRequestCreateDto();
        own1.setDescription("Мой запрос 1");
        itemRequestService.create(requester.getId(), own1);

        Thread.sleep(5);

        ItemRequestCreateDto own2 = new ItemRequestCreateDto();
        own2.setDescription("Мой запрос 2");
        itemRequestService.create(requester.getId(), own2);

        ItemRequestCreateDto o1 = new ItemRequestCreateDto();
        o1.setDescription("Нужна дрель");
        ItemRequestDto r1 = itemRequestService.create(other1.getId(), o1);

        Thread.sleep(5);

        ItemRequestCreateDto o2 = new ItemRequestCreateDto();
        o2.setDescription("Нужна болгарка");
        ItemRequestDto r2 = itemRequestService.create(other2.getId(), o2);

        Thread.sleep(5);

        ItemRequestCreateDto o3 = new ItemRequestCreateDto();
        o3.setDescription("Нужен перфоратор");
        ItemRequestDto r3 = itemRequestService.create(other1.getId(), o3);

        List<ItemRequestDto> page1 = itemRequestService.getAllRequests(requester.getId(), 0, 2);
        assertThat(page1).hasSize(2);
        assertThat(page1.get(0).getId()).isEqualTo(r3.getId());
        assertThat(page1.get(1).getId()).isEqualTo(r2.getId());
        assertThat(page1.stream().allMatch(dto ->
                !dto.getDescription().startsWith("Мой запрос"))).isTrue();

        List<ItemRequestDto> page2 = itemRequestService.getAllRequests(requester.getId(), 2, 2);
        assertThat(page2).hasSize(1);
        assertThat(page2.get(0).getId()).isEqualTo(r1.getId());
        assertThat(page2.stream().allMatch(dto ->
                !dto.getDescription().startsWith("Мой запрос"))).isTrue();
    }

    @Test
    void getById_returnsWithAnswers_ok() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("Нужна отвертка с трещоткой");
        ItemRequestDto created = itemRequestService.create(requester.getId(), dto);

        ItemRequestDto found = itemRequestService.getRequestById(requester.getId(), created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getDescription()).isEqualTo("Нужна отвертка с трещоткой");
        assertThat(found.getCreated()).isNotNull();

        assertThat(found.getItems()).isEmpty();
    }
}
