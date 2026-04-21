package com.example.board.dto;

import java.time.LocalDateTime;

public class BoardDto {
    private final Long id;
    private final String name;
    private final LocalDateTime createdAt;

    public BoardDto(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
