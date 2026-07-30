package com.example.bdsqltester.dtos;

public class MetodeItem {
    private final int id;
    private final String label;

    public MetodeItem(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
