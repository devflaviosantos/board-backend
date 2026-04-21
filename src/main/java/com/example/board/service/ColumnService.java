package com.example.board.service;

import com.example.board.config.ResourceNotFoundException;
import com.example.board.dto.CardDto;
import com.example.board.dto.ColumnDto;
import com.example.board.dto.ColumnRequest;
import com.example.board.model.Board;
import com.example.board.model.BoardColumn;
import com.example.board.model.Card;
import com.example.board.repository.BoardRepository;
import com.example.board.repository.ColumnRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final BoardRepository boardRepository;

    public ColumnService(ColumnRepository columnRepository, BoardRepository boardRepository) {
        this.columnRepository = columnRepository;
        this.boardRepository = boardRepository;
    }

    @Transactional(readOnly = true)
    public List<ColumnDto> findByBoard(Long boardId) {
        Board board = findBoard(boardId);
        return columnRepository.findByBoardOrderByPositionAsc(board).stream()
                .map(this::toDto)
                .toList();
    }

    public ColumnDto create(Long boardId, ColumnRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name: não pode ser em branco");
        }
        Board board = findBoard(boardId);
        int nextPosition = columnRepository.findMaxPositionByBoardId(boardId).orElse(0) + 1;

        BoardColumn column = new BoardColumn();
        column.setBoard(board);
        column.setName(request.getName());
        column.setPosition(nextPosition);
        return toDto(columnRepository.save(column));
    }

    public ColumnDto update(Long id, ColumnRequest request) {
        BoardColumn column = findColumn(id);
        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name: não pode ser em branco");
            }
            column.setName(request.getName());
        }
        if (request.getPosition() != null) {
            column.setPosition(request.getPosition());
        }
        return toDto(columnRepository.save(column));
    }

    public void delete(Long id) {
        columnRepository.delete(findColumn(id));
    }

    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board não encontrado com id: " + boardId));
    }

    private BoardColumn findColumn(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna não encontrada com id: " + id));
    }

    private ColumnDto toDto(BoardColumn column) {
        List<CardDto> cards = column.getCards().stream()
                .map(this::toCardDto)
                .toList();
        return new ColumnDto(
                column.getId(), column.getBoard().getId(),
                column.getName(), column.getPosition(), cards);
    }

    private CardDto toCardDto(Card card) {
        return new CardDto(
                card.getId(), card.getColumn().getId(), card.getTitle(),
                card.getDescription(), card.getLabel(), card.getPosition(),
                card.isCompleted(), card.getCreatedAt());
    }
}
