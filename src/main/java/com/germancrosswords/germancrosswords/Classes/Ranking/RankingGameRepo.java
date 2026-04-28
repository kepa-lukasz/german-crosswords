package com.germancrosswords.germancrosswords.Classes.Ranking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankingGameRepo extends JpaRepository<RankingGame, Long> {
    List<RankingGame> findByUserIdOrderByCreatedAtDesc(Long userId);
    
}