package com.example.bdsqltester.scenes.adminPusat;

import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Cabang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class pusatController {

    @FXML private TableView<Cabang> cabang_table;
    @FXML private TableColumn<Cabang, String> cabangName;
    @FXML private TableColumn<Cabang, Void> reviewButton;
    @FXML private TableColumn<Cabang, Void> actionButton;

    private final ObservableList<Cabang> cabangList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cabangName.setCellValueFactory(new PropertyValueFactory<>("cabangNama"));

        reviewButton.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Review");
            {
                btn.setStyle("-fx-background-color:#FF9F1C; -fx-text-fill:white; -fx-font-weight:bold;");
                btn.setOnAction(e -> openReview(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });

        actionButton.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Cabang cabang = getTableView().getItems().get(getIndex());
                setStyleButton(cabang);
                btn.setOnAction(e -> {
                    String target = cabang.isCabangStatus() ? "Nonaktif" : "Aktif";
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Jadikan cabang " + target + "?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.YES) toggleStatus(cabang);
                    });
                });
                setGraphic(btn);
                setAlignment(Pos.CENTER);
            }
            private void setStyleButton(Cabang c) {
                boolean active = c.isCabangStatus();
                btn.setText(active ? "Aktif" : "Nonaktif");
                btn.setStyle(active ? "-fx-background-color:#2ECC71; -fx-text-fill:white; -fx-font-weight:bold;"
                        : "-fx-background-color:#E74C3C; -fx-text-fill:white; -fx-font-weight:bold;");
            }
        });

        loadCabangData();
    }

    private void loadCabangData() {
        cabangList.clear();
        String query = "SELECT cabang_id, cabang_nama, cabang_alamat, cabang_kontak, cabang_status FROM Cabang";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cabangList.add(new Cabang(
                        rs.getLong("cabang_id"),
                        rs.getString("cabang_nama"),
                        rs.getString("cabang_alamat"),
                        rs.getString("cabang_kontak"),
                        rs.getBoolean("cabang_status")));
            }
            cabang_table.setItems(cabangList);
        } catch (SQLException e) {
            showError("Gagal memuat data cabang.");
        }
    }

    private void toggleStatus(Cabang cabang) {
        try (Connection conn = DataSourceManager.getUserConnection()) {
            String sql = "UPDATE Cabang SET cabang_status = ? WHERE cabang_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setBoolean(1, !cabang.isCabangStatus());
            ps.setLong(2, cabang.getCabangID());
            ps.executeUpdate();
            loadCabangData();
        } catch (SQLException e) {
            showError("Gagal mengubah status cabang.");
        }
    }

    private void openReview(Cabang cabang) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/adminPusat/centralViewBranch.fxml"));
            Parent root = loader.load();
            centralViewbController controller = loader.getController();
            controller.setCabang(cabang);
            Stage stage = new Stage();
            stage.setTitle("Central View Branch");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Gagal membuka halaman cabang.");
        }
    }

    public void handleAddCabang() {
        Dialog<Cabang> dialog = buildCabangDialog("Tambah Cabang", null);
        dialog.showAndWait().ifPresent(newCabang -> {
            try (Connection checkConn = DataSourceManager.getUserConnection()) {
                PreparedStatement check = checkConn.prepareStatement("SELECT 1 FROM cabang WHERE cabang_nama = ?");
                check.setString(1, newCabang.getCabangNama());
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    showError("Nama cabang sudah digunakan.");
                    return;
                }
            } catch (SQLException e) {
                showError("Gagal memeriksa nama cabang.");
                return;
            }
            try (Connection conn = DataSourceManager.getUserConnection()) {
                String sql = "INSERT INTO Cabang (cabang_nama, cabang_alamat, cabang_kontak, cabang_status) VALUES (?,?,?,TRUE)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, newCabang.getCabangNama());
                ps.setString(2, newCabang.getCabangAlamat());
                ps.setString(3, newCabang.getCabangKontak());
                ps.executeUpdate();
                loadCabangData();
            } catch (SQLException e) {
                showError("Gagal menambah cabang.");
            }
        });
    }

    public void handleEditCabang() {
        Cabang selected = cabang_table.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Pilih cabang yang ingin diedit."); return; }
        Dialog<Cabang> dialog = buildCabangDialog("Edit Cabang", selected);
        dialog.showAndWait().ifPresent(updated -> {
            try (Connection checkConn = DataSourceManager.getUserConnection()) {
                PreparedStatement check = checkConn.prepareStatement(
                        "SELECT 1 FROM cabang WHERE cabang_nama = ? AND cabang_id <> ?");
                check.setString(1, updated.getCabangNama());
                check.setLong(2, selected.getCabangID());
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    showError("Nama cabang sudah digunakan.");
                    return;
                }
            } catch (SQLException e) {
                showError("Gagal memeriksa nama cabang.");
                return;
            }
            try (Connection conn = DataSourceManager.getUserConnection()) {
                String sql = "UPDATE Cabang SET cabang_nama = ?, cabang_alamat = ?, cabang_kontak = ? WHERE cabang_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, updated.getCabangNama());
                ps.setString(2, updated.getCabangAlamat());
                ps.setString(3, updated.getCabangKontak());
                ps.setLong(4, selected.getCabangID());
                ps.executeUpdate();
                loadCabangData();
            } catch (SQLException e) {
                showError("Gagal mengedit cabang.");
            }
        });
    }

    private Dialog<Cabang> buildCabangDialog(String title, Cabang existing) {
        Dialog<Cabang> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        TextField namaField = new TextField(); namaField.setPromptText("Nama Cabang");
        TextField alamatField = new TextField(); alamatField.setPromptText("Alamat Cabang");
        TextField kontakField = new TextField(); kontakField.setPromptText("Kontak Cabang");
        if (existing != null) {
            namaField.setText(existing.getCabangNama());
            alamatField.setText(existing.getCabangAlamat());
            kontakField.setText(existing.getCabangKontak());
        }
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.addRow(0, new Label("Nama:"), namaField);
        grid.addRow(1, new Label("Alamat:"), alamatField);
        grid.addRow(2, new Label("Kontak:"), kontakField);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String nama = namaField.getText().trim();
                String kontak = kontakField.getText().trim();
                String alamat = alamatField.getText().trim();
                if (nama.isEmpty() || kontak.isEmpty() || alamat.isEmpty()) {
                    showError("Field tidak boleh kosong.");
                    return null;
                }
                if (!kontak.matches("\\d+")) { showError("Kontak cabang harus berupa angka!"); return null; }
                if (!(kontak.length()==12)) {
                    showError("No Telp harus 12 digit");
                    return null;
                }
                if (alamat.length() < 4) {
                    showError("Alamat harus lebih dari 3 karakter.");
                    return null;
                }
                return new Cabang(existing != null ? existing.getCabangID() : 0,
                        nama, alamat, kontak, true);
            }
            return null;
        });
        return dialog;
    }

    private void showError(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK);
        a.setHeaderText(null); a.setTitle("Terjadi Kesalahan"); a.showAndWait();
    }
}
