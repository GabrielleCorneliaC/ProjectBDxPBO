package com.example.bdsqltester.scenes.adminCabang;

import com.example.bdsqltester.HelloApplication;
import javafx.fxml.FXML;

public class AdminNavbarController {

    @FXML
    public void nextMenu() {
        HelloApplication.getApplicationInstance().switchScene("adminCabang/menuadminCabang-view.fxml", "Menu");
    }

    @FXML
    public void nextJadwal() {
        HelloApplication.getApplicationInstance().switchScene("adminCabang/jadwal-view.fxml", "Jadwal");
    }

    @FXML
    public void nextOrder() {
        HelloApplication.getApplicationInstance().switchScene("adminCabang/processOrder-view.fxml", "Orders");
    }
}
