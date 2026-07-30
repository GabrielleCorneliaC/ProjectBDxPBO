package com.example.bdsqltester.dtos;

public class JadwalItem {
    private final int id;
    private final String label;

    public JadwalItem(int id, String label) {
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
        return label; // This is what shows in the ComboBox
    }
}
