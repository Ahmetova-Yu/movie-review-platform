package com.moviereview.filmservice.repository;

import com.moviereview.filmservice.entity.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FilmRepository extends JpaRepository<Film, Long> {
    Optional<Film> findByTitle(String title);

    @Query("SELECT f FROM Film f ORDER BY f.averageRating DESC")
    List<Film> findTopFilms();

    @Query("SELECT f FROM Film f WHERE LOWER(f.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Film> searchByTitle(@Param("keyword") String keyword);
}