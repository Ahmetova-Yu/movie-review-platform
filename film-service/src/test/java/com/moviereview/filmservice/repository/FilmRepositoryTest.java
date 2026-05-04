package com.moviereview.filmservice.repository;

import com.moviereview.filmservice.entity.Film;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FilmRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private FilmRepository filmRepository;

    @Test
    void saveFilm_shouldGenerateId() {
        // given
        Film film = Film.builder()
                .title("Test Film")
                .originalTitle("Test Original")
                .releaseYear(2024)
                .genres(Set.of("Action", "Drama"))
                .build();

        // when
        Film saved = filmRepository.save(film);

        // then
        assertNotNull(saved.getId());
        assertEquals("Test Film", saved.getTitle());
    }

    @Test
    void findByTitle_shouldReturnFilm() {
        // given
        Film film = Film.builder()
                .title("Unique Title")
                .originalTitle("Unique")
                .releaseYear(2023)
                .build();
        filmRepository.save(film);

        // when
        var found = filmRepository.findByTitle("Unique Title");

        // then
        assertTrue(found.isPresent());
        assertEquals("Unique Title", found.get().getTitle());
    }

    @Test
    void findTopFilms_shouldReturnSortedByRating() {
        // given
        Film film1 = Film.builder()
                .title("Best Film")
                .averageRating(9.5)
                .releaseYear(2020)
                .build();
        Film film2 = Film.builder()
                .title("Good Film")
                .averageRating(8.0)
                .releaseYear(2021)
                .build();
        Film film3 = Film.builder()
                .title("Average Film")
                .averageRating(7.0)
                .releaseYear(2022)
                .build();

        filmRepository.saveAll(List.of(film1, film2, film3));

        // when
        List<Film> topFilms = filmRepository.findTopFilms();

        // then
        assertEquals(3, topFilms.size());
        assertEquals("Best Film", topFilms.get(0).getTitle());
        assertEquals(9.5, topFilms.get(0).getAverageRating());
    }

    @Test
    void searchByTitle_shouldReturnMatchingFilms() {
        // given
        Film film1 = Film.builder()
                .title("The Matrix")
                .releaseYear(1999)
                .build();
        Film film2 = Film.builder()
                .title("The Matrix Reloaded")
                .releaseYear(2003)
                .build();
        Film film3 = Film.builder()
                .title("Inception")
                .releaseYear(2010)
                .build();

        filmRepository.saveAll(List.of(film1, film2, film3));

        // when
        List<Film> result = filmRepository.searchByTitle("matrix");

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(f -> f.getTitle().toLowerCase().contains("matrix")));
    }
}