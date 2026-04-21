package com.example.board.repository;

import com.example.board.model.BoardColumn;
import com.example.board.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByColumnOrderByPositionAsc(BoardColumn column);

    @Query("SELECT COALESCE(MAX(c.position), 0) FROM Card c WHERE c.column.id = :columnId")
    Optional<Integer> findMaxPositionByColumnId(@Param("columnId") Long columnId);
}
