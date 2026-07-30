package com.example.bdsqltester.dtos;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Menu {
    private int menuId;
    private String menuNama;
    private int menuHarga;
    private int menuStok;
    private boolean menuStatus;

    public Menu(int menuId, String menuNama, int menuHarga, int menuStok, boolean menuStatus) {
        this.menuId = menuId;
        this.menuNama = menuNama;
        this.menuHarga = menuHarga;
        this.menuStok = menuStok;
        this.menuStatus = menuStatus;
    }

    public Menu(ResultSet rs) {
        try {
            this.menuId = rs.getInt("menu_id");
            this.menuNama = rs.getString("menu_nama");
            this.menuHarga = rs.getInt("menu_harga");
            this.menuStok = rs.getInt("menu_stok");
            this.menuStatus = rs.getBoolean("menu_status");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMenuId() {
        return menuId;
    }

    public String getMenuNama() {
        return menuNama;
    }

    public int getMenuHarga() {
        return menuHarga;
    }

    public int getMenuStok() {
        return menuStok;
    }

    public boolean isMenuStatus() {
        return menuStatus;
    }

    public void setMenuStatus(boolean menuStatus) {
        this.menuStatus = menuStatus;
    }

    @Override
    public String toString() {
        return menuNama + " - Rp " + menuHarga;
    }
}
