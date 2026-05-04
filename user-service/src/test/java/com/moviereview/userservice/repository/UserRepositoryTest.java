package com.moviereview.userservice.repository;

import com.moviereview.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveUser_shouldGenerateId() {
        // given
        User user = User.builder()
                .username("testuser")
                .email("test@mail.com")
                .role("USER")
                .build();

        // when
        User saved = userRepository.save(user);

        // then
        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        // given
        User user = User.builder()
                .username("uniqueuser")
                .email("unique@mail.com")
                .role("USER")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        Optional<User> found = userRepository.findByUsername("uniqueuser");

        // then
        assertTrue(found.isPresent());
        assertEquals("uniqueuser", found.get().getUsername());
    }

    @Test
    void existsByUsername_whenUserExists_shouldReturnTrue() {
        // given
        User user = User.builder()
                .username("existinguser")
                .email("existing@mail.com")
                .role("USER")
                .build();
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByUsername("existinguser");

        // then
        assertTrue(exists);
    }

    @Test
    void updateReputation_shouldIncreaseReputation() {
        // given
        User user = User.builder()
                .username("reputationUser")
                .email("reputation@mail.com")
                .role("USER")
                .reputation(10)
                .build();
        User saved = userRepository.save(user);

        // when
        userRepository.updateReputation(saved.getId(), 5);
        entityManager.flush();
        entityManager.clear();

        // then
        User updated = userRepository.findById(saved.getId()).get();
        assertEquals(15, updated.getReputation());
    }
}