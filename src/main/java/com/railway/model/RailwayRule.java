package com.railway.model;

/**
 * Represents official railway travel guidelines, baggage rules, and refund policies.
 */
public class RailwayRule {
    private long id;
    private String category;
    private String title;
    private String content;
    private boolean active;

    public RailwayRule() {
    }

    public RailwayRule(long id, String category, String title, String content, boolean active) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.content = content;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "[" + category + "] " + title;
    }
}
