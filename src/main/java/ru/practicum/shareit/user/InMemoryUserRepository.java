package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден: " + id);
        }
        users.put(id, user);
        return user;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public User findById(Long id) {
        User u = users.get(id);
        if (u == null) throw new NotFoundException("Пользователь не найден: " + id);
        return u;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
