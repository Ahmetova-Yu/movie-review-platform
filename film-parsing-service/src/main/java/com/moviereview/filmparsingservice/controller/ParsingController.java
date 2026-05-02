package com.moviereview.filmparsingservice.controller;

import com.moviereview.common.dto.FilmDTO;
import com.moviereview.filmparsingservice.service.ImdbParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parse")
@RequiredArgsConstructor
@Slf4j
public class ParsingController {

    private final ImdbParsingService parsingService;

    @PostMapping("/popular")
    public ResponseEntity<List<FilmDTO>> parsePopularFilms(@RequestParam(defaultValue = "50") int count) {
        log.info("POST /parse/popular - запуск парсинга {} фильмов", count);
        return ResponseEntity.ok(parsingService.parsePopularFilms(count));
    }
}