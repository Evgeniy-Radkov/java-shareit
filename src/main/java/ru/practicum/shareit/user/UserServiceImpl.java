package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        validateUser(user);
        validateEmailUnique(user.getEmail(), null);
        return userRepository.create(user);
    }

    @Override
    public User update(User user) {
        validateUser(user);
        if (user.getId() == null) {
            throw new ValidationException("Id обязателен для обновления");
        }

        User existing = userRepository.findById(user.getId());

        String mergedName  = (user.getName()  != null) ? user.getName().trim()  : existing.getName();
        String mergedEmail = (user.getEmail() != null) ? user.getEmail().trim() : existing.getEmail();

        validateEmailUnique(mergedEmail, user.getId());

        existing.setName(mergedName);
        existing.setEmail(mergedEmail);
        return userRepository.update(existing);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }
    }

    private void validateEmailUnique(String email, Long excludeUserId) {
        for (User other : userRepository.findAll()) {
            if (other.getEmail() != null
                    && other.getEmail().equalsIgnoreCase(email)
                    && (excludeUserId == null || !other.getId().equals(excludeUserId))) {
                throw new ConflictException("Email уже используется");
            }
        }
    }
}
