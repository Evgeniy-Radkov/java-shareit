package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto dto) {
        if (dto.getEmail() != null) {
            validateEmailUnique(dto.getEmail(), null);
        }
        User toSave = userMapper.toUser(dto);
        User saved = userRepository.create(toSave);
        return userMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        User existing = userRepository.findById(id);

        if (patch.getEmail() != null) {
            validateEmailUnique(patch.getEmail(), id);
        }

        patch.setId(null);

        userMapper.updateUserFromDto(patch, existing);

        User updated = userRepository.update(existing);
        return userMapper.toUserDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (userRepository.findAll().stream().noneMatch(u -> u.getId().equals(id))) {
            throw new NotFoundException("Пользователь не найден: " + id);
        }
        userRepository.delete(id);
    }

    @Override
    public UserDto findById(Long id) {
        return userMapper.toUserDto(userRepository.findById(id));
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    //При переходе на бд в схеме следует добавить ограничение UNIQUE на поле email.
    // Получается данная проверка будет уже избыточной. Или я что-то не правильно думаю?
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
