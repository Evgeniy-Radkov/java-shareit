package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIT {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        userRepository.deleteAll();

        User u = new User();
        u.setName("Owner");
        u.setEmail("owner@example.com");
        owner = userRepository.save(u);
    }

    @Test
    void create_persists_ok() {
        ItemDto dto = new ItemDto();
        dto.setName("Перфоратор");
        dto.setDescription("Ударный режим");
        dto.setAvailable(true);

        ItemDto created = itemService.create(dto, owner.getId());

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Перфоратор");
        assertThat(created.getDescription()).isEqualTo("Ударный режим");
        assertThat(created.getAvailable()).isTrue();

        Optional<Item> fromDb = itemRepository.findById(created.getId());
        assertThat(fromDb).isPresent();

        Item entity = fromDb.get();
        assertThat(entity.getName()).isEqualTo("Перфоратор");
        assertThat(entity.getDescription()).isEqualTo("Ударный режим");
        assertThat(entity.getAvailable()).isTrue();
        assertThat(entity.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void search_blankText_returnsEmpty_ok() {
        List<ItemDto> r1 = itemService.search("");
        List<ItemDto> r2 = itemService.search("   ");
        List<ItemDto> r3 = itemService.search(null);

        assertThat(r1).isEmpty();
        assertThat(r2).isEmpty();
        assertThat(r3).isEmpty();
    }

    @Test
    void update_ownerPartialMerge_ok() {
        ItemDto dto = new ItemDto();
        dto.setName("Перфоратор");
        dto.setDescription("Ударный режим");
        dto.setAvailable(true);
        ItemDto created = itemService.create(dto, owner.getId());

        ItemDto patch = new ItemDto();
        patch.setName("Перфоратор PRO");
        ItemDto updated = itemService.update(patch, created.getId(), owner.getId());

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Перфоратор PRO");
        assertThat(updated.getDescription()).isEqualTo("Ударный режим");
        assertThat(updated.getAvailable()).isTrue();
    }

    @Test
    void addComment_onlyAfterPastApprovedBooking_ok() {
        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        ItemDto dto = new ItemDto();
        dto.setName("Перфоратор");
        dto.setDescription("С ударным режимом");
        dto.setAvailable(true);
        ItemDto createdItem = itemService.create(dto, owner.getId());

        Item itemEntity = itemRepository.findById(createdItem.getId()).orElseThrow();

        Booking past = new Booking();
        past.setItem(itemEntity);
        past.setBooker(booker);
        past.setStart(LocalDateTime.now().minusDays(2));
        past.setEnd(LocalDateTime.now().minusDays(1));
        past.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(past);

        CommentCreateDto commentCreate = new CommentCreateDto();
        commentCreate.setText("Отличный инструмент!");

        CommentDto saved = itemService.addComment(booker.getId(), createdItem.getId(), commentCreate);

        assertThat(saved).isNotNull();
        assertThat(saved.getText()).isEqualTo("Отличный инструмент!");
        assertThat(saved.getAuthorName()).isEqualTo(booker.getName());

        Optional<Comment> fromDb = commentRepository.findById(saved.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getItem().getId()).isEqualTo(createdItem.getId());
        assertThat(fromDb.get().getAuthor().getId()).isEqualTo(booker.getId());
    }
}
