package com.example.board.controller;

import com.example.board.dto.ColumnDto;
import com.example.board.dto.ColumnRequest;
import com.example.board.service.ColumnService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ColumnController {

    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @GetMapping("/api/boards/{boardId}/columns")
    public List<ColumnDto> findByBoard(@PathVariable Long boardId) {
        return columnService.findByBoard(boardId);
    }

    @PostMapping("/api/boards/{boardId}/columns")
    @ResponseStatus(HttpStatus.CREATED)
    public ColumnDto create(@PathVariable Long boardId, @RequestBody ColumnRequest request) {
        return columnService.create(boardId, request);
    }

    @PatchMapping("/api/columns/{id}")
    public ColumnDto update(@PathVariable Long id, @RequestBody ColumnRequest request) {
        return columnService.update(id, request);
    }

    @DeleteMapping("/api/columns/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        columnService.delete(id);
    }
}
