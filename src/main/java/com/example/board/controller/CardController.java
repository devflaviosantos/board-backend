package com.example.board.controller;

import com.example.board.dto.CardDto;
import com.example.board.dto.CardRequest;
import com.example.board.dto.CardUpdateRequest;
import com.example.board.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/api/columns/{columnId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardDto create(@PathVariable Long columnId, @Valid @RequestBody CardRequest request) {
        return cardService.create(columnId, request);
    }

    @PatchMapping("/api/cards/{id}")
    public CardDto update(@PathVariable Long id, @RequestBody CardUpdateRequest request) {
        return cardService.update(id, request);
    }

    @DeleteMapping("/api/cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cardService.delete(id);
    }
}
