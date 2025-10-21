package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserDto dto) {
        try {
            User toSave = userMapper.toUser(dto);
            User saved = userRepository.save(toSave);
            return userMapper.toUserDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email уже используется");
        }
    }

    @Override
    public UserDto update(Long id, UserDto patch) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));

        patch.setId(null);

        try {
            userMapper.updateUserFromDto(patch, existing);
            User saved = userRepository.save(existing);
            return userMapper.toUserDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email уже используется");
        }
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
        return userMapper.toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

}
