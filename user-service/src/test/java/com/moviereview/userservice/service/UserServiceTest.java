package com.moviereview.userservice.service;

import com.moviereview.common.dto.UserDTO;
import com.moviereview.userservice.entity.User;
import com.moviereview.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.moviereview.userservice.service.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("anna")
                .email("anna@mail.com")
                .role("USER")
                .build();

        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("anna")
                .email("anna@mail.com")
                .role("USER")
                .build();
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        UserDTO result = userService.getUserById(1L);

        // then
        assertNotNull(result);
        assertEquals("anna", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenUserNotExists_shouldThrowException() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> userService.getUserById(999L));
        verify(userRepository).findById(999L);
    }

    @Test
    void createUser_withValidData_shouldSaveAndReturnUser() {
        // given
        UserDTO newUser = UserDTO.builder()
                .username("boris")
                .email("boris@mail.com")
                .role("USER")
                .build();

        User savedUser = User.builder()
                .id(2L)
                .username("boris")
                .email("boris@mail.com")
                .role("USER")
                .build();

        when(userRepository.existsByUsername("boris")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserDTO result = userService.createUser(newUser);

        // then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("boris", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_withExistingUsername_shouldThrowException() {
        // given
        UserDTO existingUser = UserDTO.builder()
                .username("anna")
                .email("new@mail.com")
                .role("USER")
                .build();

        when(userRepository.existsByUsername("anna")).thenReturn(true);

        // when & then
        assertThrows(RuntimeException.class, () -> userService.createUser(existingUser));
        verify(userRepository, never()).save(any());
    }
}