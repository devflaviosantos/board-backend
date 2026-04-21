package com.example.board.dto;

import java.util.List;

public class ColumnDto {
    private final Long id;
    private final Long boardId;
    private final String name;
    private final Integer position;
    private final List<CardDto> cards;

    public ColumnDto(Long id, Long boardId, String name, Integer position, List<CardDto> cards) {
        this.id = id;
        this.boardId = boardId;
        this.name = name;
        this.position = position;
        this.cards = cards;
    }

    public Long getId() { return id; }
    public Long getBoardId() { return boardId; }
    public String getName() { return name; }
    public Integer getPosition() { return position; }
    public List<CardDto> getCards() { return cards; }
}
