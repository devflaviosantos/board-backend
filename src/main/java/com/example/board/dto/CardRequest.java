package com.example.board.dto;

import jakarta.validation.constraints.NotBlank;

public class CardRequest {

    @NotBlank
    private String title;
    private String description;
    private String label;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
