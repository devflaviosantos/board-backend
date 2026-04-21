package com.example.board.dto;

public class CardUpdateRequest {
    private String title;
    private String description;
    private String label;
    private Integer position;
    private Long columnId;
    private Boolean completed;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Long getColumnId() { return columnId; }
    public void setColumnId(Long columnId) { this.columnId = columnId; }
    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
