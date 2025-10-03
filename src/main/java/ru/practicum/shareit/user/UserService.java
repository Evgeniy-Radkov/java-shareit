package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    User create(User user);

    User update(User user);

    void delete(Long id);

    User findById(Long id);

    List<User> findAll();
}
