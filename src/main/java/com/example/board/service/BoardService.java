package com.example.board.service;

import com.example.board.config.ResourceNotFoundException;
import com.example.board.dto.BoardDto;
import com.example.board.dto.BoardRequest;
import com.example.board.model.Board;
import com.example.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;

    public BoardService(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
    }

    @Transactional(readOnly = true)
    public List<BoardDto> findAll() {
        return boardRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public BoardDto create(BoardRequest request) {
        Board board = new Board();
        board.setName(request.getName());
        return toDto(boardRepository.save(board));
    }

    public BoardDto rename(Long id, BoardRequest request) {
        Board board = findBoard(id);
        board.setName(request.getName());
        return toDto(boardRepository.save(board));
    }

    public void delete(Long id) {
        boardRepository.delete(findBoard(id));
    }

    private Board findBoard(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board não encontrado com id: " + id));
    }

    private BoardDto toDto(Board board) {
        return new BoardDto(board.getId(), board.getName(), board.getCreatedAt());
    }
}
