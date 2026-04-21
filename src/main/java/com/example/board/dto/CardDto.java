package com.example.board.dto;

import java.time.LocalDateTime;

public class CardDto {
    private final Long id;
    private final Long columnId;
    private final String title;
    private final String description;
    private final String label;
    private final Integer position;
    private final boolean completed;
    private final LocalDateTime createdAt;

    public CardDto(Long id, Long columnId, String title, String description, String label,
                   Integer position, boolean completed, LocalDateTime createdAt) {
        this.id = id;
        this.columnId = columnId;
        this.title = title;
        this.description = description;
        this.label = label;
        this.position = position;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getColumnId() { return columnId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLabel() { return label; }
    public Integer getPosition() { return position; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
