package com.moviereview.filmservice.service;

import com.moviereview.common.dto.FilmDTO;
import com.moviereview.filmservice.entity.Film;
import com.moviereview.filmservice.repository.FilmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private FilmService filmService;

    private Film testFilm;
    private FilmDTO testFilmDTO;

    @BeforeEach
    void setUp() {
        testFilm = Film.builder()
                .id(1L)
                .title("The Shawshank Redemption")
                .originalTitle("The Shawshank Redemption")
                .description("Two imprisoned men bond over a number of years")
                .releaseYear(1994)
                .genres(Set.of("Drama"))
                .averageRating(9.3)
                .build();

        testFilmDTO = FilmDTO.builder()
                .id(1L)
                .title("The Shawshank Redemption")
                .releaseYear(1994)
                .genres(Set.of("Drama"))
                .build();
    }

    @Test
    void getFilmById_whenFilmExists_shouldReturnFilm() {
        // given
        when(filmRepository.findById(1L)).thenReturn(Optional.of(testFilm));

        // when
        FilmDTO result = filmService.getFilmById(1L);

        // then
        assertNotNull(result);
        assertEquals("The Shawshank Redemption", result.getTitle());
        verify(filmRepository).findById(1L);
    }

    @Test
    void getFilmById_whenFilmNotExists_shouldThrowException() {
        // given
        when(filmRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> filmService.getFilmById(999L));
    }

    @Test
    void createFilm_shouldSaveAndReturnFilm() {
        // given
        FilmDTO newFilm = FilmDTO.builder()
                .title("Inception")
                .originalTitle("Inception")
                .description("A thief who steals corporate secrets")
                .releaseYear(2010)
                .genres(Set.of("Sci-Fi", "Action"))
                .build();

        Film savedFilm = Film.builder()
                .id(2L)
                .title("Inception")
                .releaseYear(2010)
                .build();

        when(filmRepository.save(any(Film.class))).thenReturn(savedFilm);

        // when
        FilmDTO result = filmService.createFilm(newFilm);

        // then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("Inception", result.getTitle());
        verify(filmRepository).save(any(Film.class));
        verify(kafkaTemplate).send(eq("film-created"), anyString(), any());
    }

    @Test
    void getAllFilms_shouldReturnListOfFilms() {
        // given
        Film film2 = Film.builder()
                .id(2L)
                .title("Inception")
                .releaseYear(2010)
                .build();
        when(filmRepository.findAll()).thenReturn(Arrays.asList(testFilm, film2));

        // when
        List<FilmDTO> result = filmService.getAllFilms();

        // then
        assertEquals(2, result.size());
        assertEquals("The Shawshank Redemption", result.get(0).getTitle());
        verify(filmRepository).findAll();
    }

    @Test
    void deleteFilm_shouldCallRepositoryDelete() {
        // given
        doNothing().when(filmRepository).deleteById(1L);

        // when
        filmService.deleteFilm(1L);

        // then
        verify(filmRepository).deleteById(1L);
    }
}