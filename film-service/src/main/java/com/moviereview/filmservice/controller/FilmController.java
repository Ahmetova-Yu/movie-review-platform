package com.moviereview.filmservice.controller;

import com.moviereview.common.dto.FilmDTO;
import com.moviereview.filmservice.service.FilmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<FilmDTO> createFilm(@RequestBody FilmDTO filmDTO) {
        log.info("POST /films - создание фильма");
        return new ResponseEntity<>(filmService.createFilm(filmDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDTO> getFilmById(@PathVariable Long id) {
        log.info("GET /films/{} - получение фильма", id);
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @GetMapping
    public ResponseEntity<List<FilmDTO>> getAllFilms() {
        log.info("GET /films - получение всех фильмов");
        return ResponseEntity.ok(filmService.getAllFilms());
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDTO>> searchFilms(@RequestParam String keyword) {
        log.info("GET /films/search - поиск по ключевому слову: {}", keyword);
        return ResponseEntity.ok(filmService.searchFilms(keyword));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long id) {
        log.info("DELETE /films/{} - удаление фильма", id);
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }
}