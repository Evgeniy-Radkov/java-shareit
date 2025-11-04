package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIT {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        User o = new User();
        o.setName("Owner");
        o.setEmail("owner@example.com");
        owner = userRepository.save(o);

        User b = new User();
        b.setName("Booker");
        b.setEmail("booker@example.com");
        booker = userRepository.save(b);

        Item it = new Item();
        it.setName("Перфоратор");
        it.setDescription("Ударный");
        it.setAvailable(true);
        it.setOwner(owner);
        item = itemRepository.save(it);
    }

    @Test
    void create_valid_waiting_ok() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(5);
        LocalDateTime end = start.plusHours(2);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(start);
        dto.setEnd(end);

        BookingDto created = bookingService.create(booker.getId(), dto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);

        Optional<Booking> fromDb = bookingRepository.findById(created.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(fromDb.get().getStart()).isEqualTo(start);
        assertThat(fromDb.get().getEnd()).isEqualTo(end);
    }

    @Test
    void approve_ownerApproves_changesToApproved_ok() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(10);
        LocalDateTime end = start.plusHours(1);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(start);
        dto.setEnd(end);
        BookingDto created = bookingService.create(booker.getId(), dto);

        BookingDto approved = bookingService.approve(owner.getId(), created.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
        Optional<Booking> fromDb = bookingRepository.findById(created.getId());
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getById_visibleForOwnerAndBooker_ok() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(15);
        LocalDateTime end = start.plusHours(1);

        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(start);
        dto.setEnd(end);
        BookingDto created = bookingService.create(booker.getId(), dto);

        BookingDto asBooker = bookingService.findById(booker.getId(), created.getId());
        BookingDto asOwner = bookingService.findById(owner.getId(), created.getId());

        assertThat(asBooker.getId()).isEqualTo(created.getId());
        assertThat(asOwner.getId()).isEqualTo(created.getId());
        assertThat(asOwner.getItem().getId()).isEqualTo(item.getId());
        assertThat(asBooker.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getAllByBooker_CURRENT_filters_ok() {
        BookingCreateDto f = new BookingCreateDto();
        f.setItemId(item.getId());
        f.setStart(LocalDateTime.now().plusHours(3));
        f.setEnd(LocalDateTime.now().plusHours(4));
        bookingService.create(booker.getId(), f);

        BookingCreateDto c = new BookingCreateDto();
        c.setItemId(item.getId());
        c.setStart(LocalDateTime.now().minusMinutes(10));
        c.setEnd(LocalDateTime.now().plusMinutes(50));
        BookingDto cCreated = bookingService.create(booker.getId(), c);
        bookingService.approve(owner.getId(), cCreated.getId(), true);

        BookingCreateDto p = new BookingCreateDto();
        p.setItemId(item.getId());
        p.setStart(LocalDateTime.now().minusHours(3));
        p.setEnd(LocalDateTime.now().minusHours(2));
        BookingDto pCreated = bookingService.create(booker.getId(), p);
        bookingService.approve(owner.getId(), pCreated.getId(), true);

        List<BookingDto> current =
                bookingService.findByOwner(owner.getId(), BookingState.CURRENT, 0, 10);


        assertThat(current).isNotNull();
        assertThat(current).isNotEmpty();
        assertThat(current.stream().allMatch(b ->
                !b.getStart().isAfter(LocalDateTime.now()) && !b.getEnd().isBefore(LocalDateTime.now())
        )).isTrue();
    }

    @Test
    void getAllByOwner_WAITING_filters_ok() {
        BookingCreateDto d1 = new BookingCreateDto();
        d1.setItemId(item.getId());
        d1.setStart(LocalDateTime.now().plusMinutes(30));
        d1.setEnd(LocalDateTime.now().plusHours(2));
        BookingDto b1 = bookingService.create(booker.getId(), d1);

        BookingCreateDto d2 = new BookingCreateDto();
        d2.setItemId(item.getId());
        d2.setStart(LocalDateTime.now().plusHours(3));
        d2.setEnd(LocalDateTime.now().plusHours(4));
        BookingDto b2 = bookingService.create(booker.getId(), d2);

        BookingCreateDto d3 = new BookingCreateDto();
        d3.setItemId(item.getId());
        d3.setStart(LocalDateTime.now().plusHours(5));
        d3.setEnd(LocalDateTime.now().plusHours(6));
        BookingDto b3 = bookingService.create(booker.getId(), d3);
        bookingService.approve(owner.getId(), b3.getId(), true);

        List<BookingDto> waiting =
                bookingService.findByOwner(owner.getId(), BookingState.WAITING, 0, 10);


        assertThat(waiting).isNotNull();
        assertThat(waiting.size()).isGreaterThanOrEqualTo(2);
        assertThat(waiting.stream().allMatch(b -> b.getStatus() == BookingStatus.WAITING)).isTrue();
        assertThat(waiting.stream().noneMatch(b -> b.getId().equals(b3.getId()))).isTrue();
    }

    @Test
    void create_selfBooking_forbidden() {
        BookingCreateDto dto = new BookingCreateDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusHours(1));
        dto.setEnd(LocalDateTime.now().plusHours(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), dto))
                .isInstanceOf(ForbiddenException.class);
    }
}
