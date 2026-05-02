package com.moviereview.filmservice.service;

import com.moviereview.common.dto.FilmDTO;
import com.moviereview.common.event.FilmCreatedEvent;
import com.moviereview.filmservice.entity.Film;
import com.moviereview.filmservice.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    private final FilmRepository filmRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Transactional
    public FilmDTO createFilm(FilmDTO filmDTO) {
        log.info("Создание фильма: {}", filmDTO.getTitle());

        Film film = Film.builder()
                .title(filmDTO.getTitle())
                .originalTitle(filmDTO.getOriginalTitle())
                .description(filmDTO.getDescription())
                .releaseYear(filmDTO.getReleaseYear())
                .genres(filmDTO.getGenres())
                .posterUrl(filmDTO.getPosterUrl())
                .build();

        Film saved = filmRepository.save(film);

        FilmCreatedEvent event = FilmCreatedEvent.builder()
                .filmId(saved.getId())
                .title(saved.getTitle())
                .releaseYear(saved.getReleaseYear())
                .build();
        kafkaTemplate.send("film-created", saved.getId().toString(), event);

        return mapToDTO(saved);
    }

    @Cacheable(value = "films", key = "#id")
    public FilmDTO getFilmById(Long id) {
        log.debug("Получение фильма из БД: {}", id);
        return filmRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Фильм не найден"));
    }

    public List<FilmDTO> getAllFilms() {
        return filmRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<Void> updateFilmRatingAsync(Long filmId, Double newAverageRating, int totalRatings) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Асинхронное обновление рейтинга фильма: {}", filmId);
            filmRepository.findById(filmId).ifPresent(film -> {
                film.setAverageRating(newAverageRating);
                film.setTotalRatings(totalRatings);
                filmRepository.save(film);
            });
        }, executorService);
    }

    @CacheEvict(value = "films", key = "#id")
    @Transactional
    public void deleteFilm(Long id) {
        filmRepository.deleteById(id);
        log.info("Фильм удалён: {}", id);
    }

    public List<FilmDTO> searchFilms(String keyword) {
        return filmRepository.searchByTitle(keyword).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private FilmDTO mapToDTO(Film film) {
        return FilmDTO.builder()
                .id(film.getId())
                .title(film.getTitle())
                .originalTitle(film.getOriginalTitle())
                .description(film.getDescription())
                .releaseYear(film.getReleaseYear())
                .averageRating(film.getAverageRating())
                .genres(film.getGenres())
                .posterUrl(film.getPosterUrl())
                .build();
    }
}