package com.germancrosswords.germancrosswords.Classes.DB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepo extends JpaRepository<Word, Long> {

    @Query(value = "SELECT * FROM german_to_polish ORDER BY md5(id::text || :seed) LIMIT :count", nativeQuery = true)
    List<Word> findWordsBySeed(
            @Param("count") int count,
            @Param("seed") String seed);

    @Query(value = "SELECT * FROM german_to_polish WHERE difficulty LIKE :difficulty " +
            "ORDER BY md5(id::text || :seed) LIMIT :count", nativeQuery = true)
    List<Word> findWordsBySeedAndCriteria(
            @Param("count") int count,
            @Param("seed") String seed,
            @Param("difficulty") String difficulty);
}