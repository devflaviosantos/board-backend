package com.example.board.service;

import com.example.board.config.ResourceNotFoundException;
import com.example.board.dto.CardDto;
import com.example.board.dto.CardRequest;
import com.example.board.dto.CardUpdateRequest;
import com.example.board.model.BoardColumn;
import com.example.board.model.Card;
import com.example.board.repository.CardRepository;
import com.example.board.repository.ColumnRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final ColumnRepository columnRepository;

    public CardService(CardRepository cardRepository, ColumnRepository columnRepository) {
        this.cardRepository = cardRepository;
        this.columnRepository = columnRepository;
    }

    public CardDto create(Long columnId, CardRequest request) {
        BoardColumn column = findColumn(columnId);
        int nextPosition = cardRepository.findMaxPositionByColumnId(columnId).orElse(0) + 1;

        Card card = new Card();
        card.setColumn(column);
        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());
        card.setLabel(request.getLabel());
        card.setPosition(nextPosition);
        return toDto(cardRepository.save(card));
    }

    public CardDto update(Long id, CardUpdateRequest request) {
        Card card = findCard(id);
        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title: não pode ser em branco");
            }
            card.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            card.setDescription(request.getDescription());
        }
        if (request.getLabel() != null) {
            card.setLabel(request.getLabel());
        }
        if (request.getPosition() != null) {
            card.setPosition(request.getPosition());
        }
        if (request.getColumnId() != null) {
            card.setColumn(findColumn(request.getColumnId()));
        }
        if (request.getCompleted() != null) {
            card.setCompleted(request.getCompleted());
        }
        return toDto(cardRepository.save(card));
    }

    public void delete(Long id) {
        cardRepository.delete(findCard(id));
    }

    private BoardColumn findColumn(Long columnId) {
        return columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Coluna não encontrada com id: " + columnId));
    }

    private Card findCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão não encontrado com id: " + id));
    }

    private CardDto toDto(Card card) {
        return new CardDto(
                card.getId(), card.getColumn().getId(), card.getTitle(),
                card.getDescription(), card.getLabel(), card.getPosition(),
                card.isCompleted(), card.getCreatedAt());
    }
}
