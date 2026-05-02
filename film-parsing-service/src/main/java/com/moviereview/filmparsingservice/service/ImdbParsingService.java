package com.moviereview.filmparsingservice.service;

import com.moviereview.common.dto.FilmDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImdbParsingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ExecutorService parsingExecutor = Executors.newFixedThreadPool(20);

    public List<FilmDTO> parsePopularFilms(int count) {
        log.info("Начало параллельного парсинга {} популярных фильмов", count);

        List<String> filmIds = fetchPopularFilmIds(count);

        List<CompletableFuture<Optional<FilmDTO>>> futures = filmIds.stream()
                .map(filmId -> CompletableFuture.supplyAsync(() -> parseFilmDetails(filmId), parsingExecutor))
                .collect(Collectors.toList());

        List<FilmDTO> films = futures.stream()
                .map(CompletableFuture::join)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        log.info("Успешно распарсено {} фильмов", films.size());

        films.forEach(film -> kafkaTemplate.send("film-parsed", film.getId().toString(), film));

        return films;
    }

    private Optional<FilmDTO> parseFilmDetails(String filmId) {
        try {
            // Имитация парсинга
            Thread.sleep(new Random().nextInt(100));

            FilmDTO film = FilmDTO.builder()
                    .title("Movie " + filmId)
                    .originalTitle("Original " + filmId)
                    .description("Description of movie " + filmId)
                    .releaseYear(2018 + new Random().nextInt(7))
                    .genres(Set.of("Action", "Drama"))
                    .build();

            log.debug("Распарсен фильм: {}", film.getTitle());
            return Optional.of(film);

        } catch (Exception e) {
            log.error("Ошибка парсинга фильма {}: {}", filmId, e.getMessage());
            return Optional.empty();
        }
    }

    private List<String> fetchPopularFilmIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ids.add("tt" + String.format("%07d", i));
        }
        return ids;
    }

    @Scheduled(fixedRate = 3600000)  // Каждый час
    public void scheduledParsing() {
        log.info("Запуск планового парсинга новых фильмов");
        List<FilmDTO> newFilms = parsePopularFilms(50);
        log.info("Плановый парсинг завершён. Найдено {} новых фильмов", newFilms.size());
    }
}