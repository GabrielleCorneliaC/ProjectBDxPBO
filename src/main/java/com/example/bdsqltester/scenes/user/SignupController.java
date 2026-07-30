package com.example.bdsqltester.scenes.user;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField confirmPasswordField;


    @FXML
    public void onCreateClick() {
        String username = usernameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();


        if (username.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Semua field harus diisi.");
        }

        else if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Password tidak cocok.");
        }

        else if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email harus mengandung '@' dan '.'");
        }
        if (!(phone.length()==12)) {
            showAlert(Alert.AlertType.ERROR, "Error","Nomor telepon harus 12 digit.");
        }

        else if (!username.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Username harus dimulai dengan huruf dan bisa mengandung angka.");
        }

        else if (!phone.matches("\\d+")) {
            showAlert(Alert.AlertType.ERROR, "Error", "Nomor HP harus berupa angka.");
        }

        else {
            try {
                insertPelanggan(username, password, email, phone);
                HelloApplication app = HelloApplication.getApplicationInstance();
                app.switchScene("user/address-view.fxml", "Set Address");
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Username Sudah Dipakai");
            }
        }
    }


    private void insertPelanggan(String nama, String password, String email, String phone) throws SQLException {
        String sql = "INSERT INTO Pelanggan (pelanggan_nama, pelanggan_password, pelanggan_email, pelanggan_notelp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nama);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    HelloApplication.getApplicationInstance().setUserId(userId);
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }


}
