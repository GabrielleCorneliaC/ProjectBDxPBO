package com.example.bdsqltester.scenes.user;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddressController {
    @FXML
    private TextField alamatField;

    @FXML
    private TextField kotaField;

    @FXML
    private TextField provinsiField;

    @FXML
    private TextField kodePosField;

    @FXML
    private void onContinueClick() throws SQLException {
        String alamat = alamatField.getText().trim();
        String kota = kotaField.getText().trim();
        String provinsi = provinsiField.getText().trim();
        String kodePosText = kodePosField.getText().trim();

        if (alamat.isEmpty() || kota.isEmpty() || provinsi.isEmpty() || kodePosText.isEmpty()) {
            showAlert("Field cannot be empty", "Error", "Semua field harus diisi.");
            return;
        }

        int kodePos = -1;
        try {
            kodePos = Integer.parseInt(kodePosText);
        } catch (NumberFormatException e) {
            showAlert("Invalid Kode Pos", "Error", "Kode pos harus berupa angka.");
            return;
        }

        if (!kota.matches(".*[a-zA-Z]+.*")) {
            showAlert("Invalid Kota", "Error", "Kota harus mengandung huruf.");
            return;
        }

        if(kota.length()<4 || provinsi.length()<4 || alamat.length()<4){
            showAlert("Invalid Input", "Error", "Input Minimal 4 Huruf");
            return;
        }

        if (!provinsi.matches(".*[a-zA-Z]+.*")) {
            showAlert("Invalid Provinsi", "Error", "Provinsi harus mengandung huruf.");
            return;
        }

        if (!alamat.matches("[a-zA-Z0-9.,\\s]+")) {
            showAlert("Invalid Alamat", "Error", "Alamat hanya boleh mengandung huruf, angka, titik (.), koma (,), dan spasi.");
            return;
        }

        int userID = HelloApplication.getApplicationInstance().getUserId();

        insertAddress(alamat, kota, provinsi, kodePos, userID);
        HelloApplication app = HelloApplication.getApplicationInstance();
        app.switchScene("login-view.fxml", "Login");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }


    private void insertAddress(String alamat, String kota, String provinsi, int kodePos, int userID) throws SQLException {
        String sql = "INSERT INTO alamat (alamat_jalan, alamat_kota, alamat_provinsi, kode_pos, pelanggan_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DataSourceManager.getUserConnection()) {
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, alamat);
            stmt.setString(2, kota);
            stmt.setString(3, provinsi);
            stmt.setInt(4, kodePos);
            stmt.setInt(5, userID);
            stmt.executeUpdate();
        }
    }
}