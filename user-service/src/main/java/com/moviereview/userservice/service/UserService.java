package com.moviereview.userservice.service;

import com.moviereview.common.dto.UserDTO;
import com.moviereview.userservice.entity.User;
import com.moviereview.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Создание нового пользователя
     */
    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        log.info("Создание пользователя: {}", userDTO.getUsername());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username уже существует: " + userDTO.getUsername());
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email уже существует: " + userDTO.getEmail());
        }

        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .role(userDTO.getRole() != null ? userDTO.getRole() : "USER")
                .reputation(0)
                .reviewsCount(0)
                .likesReceived(0)
                .build();

        User saved = userRepository.save(user);
        log.info("Пользователь создан с ID: {}", saved.getId());

        return mapToDTO(saved);
    }

    /**
     * Поиск пользователя по ID
     */
    @Cacheable(value = "users", key = "#id")
    public UserDTO getUserById(Long id) {
        log.debug("Поиск пользователя по ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));

        return mapToDTO(user);
    }

    /**
     * Поиск пользователя по имени
     */
    public UserDTO getUserByUsername(String username) {
        log.debug("Поиск пользователя по имени: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь с именем " + username + " не найден"));

        return mapToDTO(user);
    }

    /**
     * Обновление репутации пользователя
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void updateReputation(Long userId, Integer delta) {
        log.info("Обновление репутации пользователя {} на {}", userId, delta);
        userRepository.updateReputation(userId, delta);
    }

    /**
     * Увеличение счётчика рецензий
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void incrementReviewsCount(Long userId) {
        log.info("Увеличение счётчика рецензий для пользователя {}", userId);
        userRepository.incrementReviewsCount(userId);
    }

    /**
     * Обновление роли пользователя
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDTO updateUserRole(Long userId, String newRole) {
        log.info("Обновление роли пользователя {} на {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setRole(newRole);
        User saved = userRepository.save(user);

        return mapToDTO(saved);
    }

    /**
     * Удаление пользователя
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteUser(Long userId) {
        log.warn("Удаление пользователя с ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        userRepository.delete(user);
        log.info("Пользователь {} удалён", user.getUsername());
    }

    /**
     * Конвертация Entity -> DTO
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}