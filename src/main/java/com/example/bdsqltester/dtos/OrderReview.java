package com.example.bdsqltester.dtos;

import javafx.beans.property.*;

public class OrderReview {
    private final StringProperty menu;
    private final IntegerProperty jumlah;
    private final IntegerProperty hargaTotal;
    private final StringProperty status;

    public OrderReview(String menu, int jumlah, int hargaTotal, String status) {
        this.menu = new SimpleStringProperty(menu);
        this.jumlah = new SimpleIntegerProperty(jumlah);
        this.hargaTotal = new SimpleIntegerProperty(hargaTotal);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty menuProperty() { return menu; }
    public IntegerProperty jumlahProperty() { return jumlah; }
    public IntegerProperty hargaTotalProperty() { return hargaTotal; }
    public StringProperty statusProperty() { return status; }
}
