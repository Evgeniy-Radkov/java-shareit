package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    UserDto update(Long id, UserDto userDto);

    void delete(Long id);

    UserDto findById(Long id);

    List<UserDto> findAll();
}
