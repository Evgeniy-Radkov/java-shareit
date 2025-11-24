package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_valid_ok() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");

        UserDto created = userService.create(dto);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();

        Optional<User> userFromDb = userRepository.findById(created.getId());
        assertThat(userFromDb).isPresent();

        User user = userFromDb.get();
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void create_duplicateEmail_throws() {
        UserDto first = new UserDto();
        first.setName("John");
        first.setEmail("john@example.com");
        userService.create(first);

        UserDto second = new UserDto();
        second.setName("Jane");
        second.setEmail("john@example.com");

        assertThatThrownBy(() -> userService.create(second))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email уже используется");
    }

    @Test
    void getById_notFound_throws() {
        long missingId = 999L;

        assertThatThrownBy(() -> userService.findById(missingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_partial_ok() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");
        UserDto created = userService.create(dto);

        UserDto updateDto = new UserDto();
        updateDto.setName("Johnny");
        updateDto.setEmail(null);
        UserDto updated = userService.update(created.getId(), updateDto);

        assertThat(updated.getName()).isEqualTo("Johnny");
        assertThat(updated.getEmail()).isEqualTo("john@example.com");

        Optional<User> userFromDb = userRepository.findById(created.getId());
        assertThat(userFromDb).isPresent();
        User user = userFromDb.get();
        assertThat(user.getName()).isEqualTo("Johnny");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
    }
}
