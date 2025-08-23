package com.example.taskgame.domain.models;

import java.util.Objects;

public class Category {
    private long id;
    private String name;
    private int color; // ARGB int (npr. Color.RED)

    public Category(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category c = (Category) o;
        return id == c.id && Objects.equals(name, c.name);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name); }
}
