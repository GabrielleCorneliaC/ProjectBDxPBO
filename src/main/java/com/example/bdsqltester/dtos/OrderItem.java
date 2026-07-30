package com.example.bdsqltester.dtos;

public class OrderItem {
    private String menu;
    private int jumlah;
    private int hargaPerItem;

    public OrderItem(String menu, int jumlah, int hargaPerItem) {
        this.menu = menu;
        this.jumlah = jumlah;
        this.hargaPerItem = hargaPerItem;
    }

    public String getMenu() { return menu; }
    public int getJumlah() { return jumlah; }
    public int getHargaPerItem() { return hargaPerItem; }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public void setHargaPerItem(int hargaPerItem) {
        this.hargaPerItem = hargaPerItem;
    }

    public String getHargaFormatted() {
        return "Rp " + String.format("%,d", jumlah * hargaPerItem).replace(',', '.');
    }

    public int getTotal() {
        return jumlah * hargaPerItem;
    }
}
