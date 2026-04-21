package com.example.board.repository;

import com.example.board.model.Board;
import com.example.board.model.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColumnRepository extends JpaRepository<BoardColumn, Long> {

    List<BoardColumn> findByBoardOrderByPositionAsc(Board board);

    @Query("SELECT COALESCE(MAX(c.position), 0) FROM BoardColumn c WHERE c.board.id = :boardId")
    Optional<Integer> findMaxPositionByBoardId(@Param("boardId") Long boardId);
}
