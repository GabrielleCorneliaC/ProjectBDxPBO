package com.example.bdsqltester.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.*;

public class LoginController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private ChoiceBox<String> selectRole;

    @FXML
    private TextField usernameField;

    private int userId;

    public int getUserId() {
        return userId;
    }

    @FXML
    void initialize() {
        selectRole.getItems().addAll("User", "Admin Cabang", "Admin Pusat");
        selectRole.setValue("User");
    }

    @FXML
    void onLoginClick(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        try {
            boolean loginSuccess = false;

            if (role.equalsIgnoreCase("User")) {
                loginSuccess = verifyTemplate(username, password,
                        "SELECT * FROM Pelanggan WHERE pelanggan_nama = ?",
                        "pelanggan_password", "pelanggan_id",
                        DataSourceManager.getUserConnection());

            } else if (role.equalsIgnoreCase("Admin Cabang")) {
                loginSuccess = verifyBranchAdmin(username, password);

            } else if (role.equalsIgnoreCase("Admin Pusat")) {
                loginSuccess = verifyTemplate(username, password,
                        "SELECT * FROM Staff WHERE staff_nama = ? AND role_id = 2",
                        "staff_password", "staff_id",
                        DataSourceManager.getCentralAdminConnection());
            }

            if (loginSuccess) {
                HelloApplication app = HelloApplication.getApplicationInstance();
                app.setUserId(this.userId);
                switch (role) {
                    case "User":
                        app.switchScene("user/order-view.fxml", "Order View");
                        break;
                    case "Admin Cabang":
                        app.switchScene("adminCabang/menuadminCabang-view.fxml", "Admin Cabang");
                        break;
                    case "Admin Pusat":
                        app.switchScene("adminPusat/centraladmin-view.fxml", "Admin Pusat");
                        break;
                }
            } else {
                showAlert("Login Failed", "Invalid Credentials", "Please check your username and password.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Connection Failed", "Could not connect to the database.");
        }
    }

    public boolean verifyTemplate(String username, String password, String sql,
                                  String columnpass, String columnid, Connection c) {
        try (c) {
            PreparedStatement stmt = c.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString(columnpass);
                if (dbPassword.equals(password)) {
                    this.userId = rs.getInt(columnid);
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean verifyBranchAdmin(String username, String password) {
        String sql = """
    SELECT s.*, c.cabang_status
    FROM staff s
    JOIN cabang c ON s.cabang_id = c.cabang_id
    WHERE s.staff_nama = ? AND s.role_id = 1
    """;

        try (Connection conn = DataSourceManager.getBranchAdminConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean statusStaff   = rs.getBoolean("staff_status");
                boolean statusCabang  = rs.getBoolean("cabang_status");

                if (!statusStaff || !statusCabang) {
                    showAlert("Akun Nonaktif", "Login Gagal",
                            "Akun Anda atau cabang Anda telah dinonaktifkan.");
                    return false;
                }

                String dbPassword = rs.getString("staff_password");
                if (dbPassword.equals(password)) {
                    this.userId = rs.getInt("staff_id");
                    int cabangId = rs.getInt("cabang_id");
                    HelloApplication.getApplicationInstance().setCabangId(cabangId);
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    @FXML
    public void onSignUpClick() throws IOException {
        HelloApplication app = HelloApplication.getApplicationInstance();
        app.switchScene("user/signup-view.fxml", "Sign Up");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
