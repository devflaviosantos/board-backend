package com.example.board.controller;

import com.example.board.dto.BoardDto;
import com.example.board.dto.BoardRequest;
import com.example.board.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<BoardDto> findAll() {
        return boardService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardDto create(@Valid @RequestBody BoardRequest request) {
        return boardService.create(request);
    }

    @PatchMapping("/{id}")
    public BoardDto rename(@PathVariable Long id, @Valid @RequestBody BoardRequest request) {
        return boardService.rename(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        boardService.delete(id);
    }
}
