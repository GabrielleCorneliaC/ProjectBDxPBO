package com.example.bdsqltester.scenes.adminPusat;

import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Cabang;
import com.example.bdsqltester.dtos.Staff;
import com.example.bdsqltester.dtos.Transaksi;
import javafx.application.Platform;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class centralViewbController {

    @FXML private Label labelNamaCabang;
    @FXML private Label labelAlamatCabang;

    @FXML private DatePicker dateFilter;
    @FXML private TableView<Transaksi> tablePesanan;

    @FXML private TableView<Staff> tableAdmin;
    @FXML private TableColumn<Staff, String> colAdminNama;
    @FXML private TableColumn<Staff, String> colAdminEmail;
    @FXML private TableColumn<Staff, String> colAdminAksi;

    @FXML private TableColumn<Staff, String> colAdminNotelp;
    @FXML private TableColumn<Staff, String> colAdminPassword;
    @FXML private TableColumn<Staff, String> colAdminTanggal;
    @FXML private TableColumn<Staff, Integer> colAdminGaji;
    @FXML private TableColumn<Staff, Integer> colAdminRole;
    @FXML private TableColumn<Staff, Integer> colAdminCabang;
    @FXML private TableColumn<Transaksi, Integer> colPesananId;
    @FXML private TableColumn<Transaksi, Date> colPesananTanggal;
    @FXML private TableColumn<Transaksi, Time> colPesananJam;
    @FXML private TableColumn<Transaksi, Integer> colPesananTotal;


    private final ObservableList<Staff> adminList = FXCollections.observableArrayList();
    private Cabang currentCabang;

    public void setCabang(Cabang cabang) {
        this.currentCabang = cabang;
        labelNamaCabang.setText(cabang.getCabangNama());
        labelAlamatCabang.setText(cabang.getCabangAlamat());
        loadPesanan(null);
        loadAdmin();
    }

    @FXML
    public void initialize() {
        colAdminNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colAdminEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colAdminNotelp.setCellValueFactory(new PropertyValueFactory<>("notelp"));
        colAdminPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colAdminTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalMasuk"));
        colAdminGaji.setCellValueFactory(new PropertyValueFactory<>("gaji"));
        colAdminRole.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        colAdminCabang.setCellValueFactory(new PropertyValueFactory<>("cabangId"));
        colPesananId.setCellValueFactory(new PropertyValueFactory<>("transaksiId"));
        colPesananTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colPesananJam.setCellValueFactory(new PropertyValueFactory<>("jam"));
        colPesananTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colAdminRole.setVisible(false);
        colAdminCabang.setVisible(false);

        colAdminAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("Edit");
            private final Button btnStatus = new Button();
            private final HBox box = new HBox(5, btnEdit, btnStatus);

            {
                btnEdit.setStyle("-fx-background-color:#FF9F1C;-fx-text-fill:white;-fx-font-weight:bold;");
                btnStatus.setStyle("-fx-background-color:#FF9F1C;-fx-text-fill:white;-fx-font-weight:bold;");

                btnEdit.setOnAction(e -> {
                    Staff s = getTableView().getItems().get(getIndex());
                    showEditDialog(s);
                });

                btnStatus.setOnAction(e -> {
                    Staff s = getTableView().getItems().get(getIndex());
                    boolean active = s.isStaffStatus();
                    String target = active ? "Nonaktif" : "Aktif";
                    Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                            "Jadikan admin " + target + "?", ButtonType.OK, ButtonType.CANCEL);
                    c.setHeaderText(null);
                    c.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) toggleAdminStatus(s.getStaffId(), !active);
                    });
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Staff s = getTableView().getItems().get(getIndex());
                    btnStatus.setText(s.isStaffStatus() ? "Nonaktifkan" : "Aktifkan");
                    setGraphic(box);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        Platform.runLater(() -> {
            tablePesanan.lookup(".column-header-background").setStyle("-fx-background-color:#178C83;");
            tableAdmin.lookup(".column-header-background").setStyle("-fx-background-color:#178C83;");
            tablePesanan.lookupAll(".label").forEach(l ->
                    l.setStyle("-fx-text-fill:#FF9F1C;-fx-font-weight:bold;-fx-font-family:'Segoe UI';"));
            tableAdmin.lookupAll(".label").forEach(l ->
                    l.setStyle("-fx-text-fill:#FF9F1C;-fx-font-weight:bold;-fx-font-family:'Segoe UI';"));
        });
    }

    private final ObservableList<Transaksi> pesananList = FXCollections.observableArrayList();

    private void loadPesanan(LocalDate filterDate) {
        pesananList.clear();

        String sql = """
        SELECT transaksi_id, transaksi_tanggal, transaksi_jam, total_pembayaran
        FROM transaksi
        WHERE cabang_id = ?
    """ + (filterDate != null ? " AND transaksi_tanggal = ?" : "");

        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, (int) currentCabang.getCabangID());

            if (filterDate != null) {
                stmt.setDate(2, Date.valueOf(filterDate));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaksi t = new Transaksi(
                        rs.getInt("transaksi_id"),
                        rs.getDate("transaksi_tanggal"),
                        rs.getTime("transaksi_jam"),
                        rs.getInt("total_pembayaran"));
                pesananList.add(t);
            }

            tablePesanan.setItems(pesananList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLaporan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/adminPusat/laporan-view.fxml"));
            Parent root = loader.load();

             LaporanController controller = loader.getController();
             controller.setCabang(currentCabang);

            Stage stage = new Stage();
            stage.setTitle("Laporan Cabang");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Gagal membuka halaman laporan: " + e.getMessage());}
    }


    private void loadAdmin() {
        adminList.clear();
        String sql = """
            SELECT staff_id, staff_nama, staff_email, notelp, staff_password,
                   tanggal_masuk, gaji, role_id, cabang_id, staff_status
            FROM staff
            WHERE cabang_id = ?
            """;
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, (int) currentCabang.getCabangID());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Staff s = new Staff(
                        rs.getInt("staff_id"),
                        rs.getString("staff_nama"),
                        rs.getString("staff_email"),
                        rs.getString("notelp"),
                        rs.getString("staff_password"),
                        rs.getString("tanggal_masuk"),
                        rs.getInt("gaji"),
                        rs.getInt("role_id"),
                        rs.getInt("cabang_id"));
                s.setStaffStatus(rs.getBoolean("staff_status"));
                adminList.add(s);
            }
            tableAdmin.setItems(adminList);
        } catch (SQLException e) { e.printStackTrace(); }
    }


    private void showEditDialog(Staff staff) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Admin");

        Label labelEmail = new Label("Email:");
        TextField tfEmail = new TextField(staff.getEmail());
        Label labelNo = new Label("No Telp:");
        TextField tfNo = new TextField(staff.getNotelp());
        Label labelGaji = new Label("Gaji:");
        TextField tfGaji = new TextField(String.valueOf(staff.getGaji()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, labelEmail, tfEmail);
        grid.addRow(1, labelNo, tfNo);
        grid.addRow(2, labelGaji, tfGaji);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {

                String email = tfEmail.getText().trim();
                String noTelp = tfNo.getText().trim();
                String gajiStr = tfGaji.getText().trim();

                if (!noTelp.matches("\\d+")) {
                    showAlert("Nomor telepon harus berupa angka dan harus 12 angka.");
                    return null;
                }
                if (!(noTelp.length() == 12)) {
                    showAlert("Nomor telepon harus 12 digit.");
                    return null;
                }

                try {
                    double gaji = Double.parseDouble(gajiStr);
                    if (gaji <= 0) {
                        showAlert("Gaji harus lebih dari 0.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Gaji harus berupa angka yang valid.");
                    return null;
                }

                int roleStr = 1;
                updateAdminInfoFull(staff.getStaffId(), email, noTelp, gajiStr, String.valueOf(roleStr));
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateAdminInfoFull(int staffId, String email, String notelp, String gajiStr, String roleStr) {
        String sql = "UPDATE staff SET staff_email = ?, notelp = ?, gaji = ?, role_id = 1 WHERE staff_id = ?";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, notelp);
            stmt.setInt(3, Integer.parseInt(gajiStr));
            stmt.setInt(5, staffId);
            stmt.executeUpdate();
            loadAdmin();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void toggleAdminStatus(int staffId, boolean newStatus) {
        String sql = "UPDATE staff SET staff_status = ? WHERE staff_id = ?";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, newStatus);
            stmt.setInt(2, staffId);
            stmt.executeUpdate();
            loadAdmin();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Gagal mengubah status admin.");
        }
    }

    @FXML
    private void handleDiskon() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Diskon Cabang");

        TextField tfPersen = new TextField();
        tfPersen.setPromptText("Masukkan diskon (%)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Diskon (%):"), tfPersen);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                String diskonText = tfPersen.getText().trim();

                if (!diskonText.matches("\\d+")) {
                    showAlert("Diskon harus berupa angka!");
                    return null;
                }

                int diskon = Integer.parseInt(diskonText);

                if (diskon < 0 || diskon > 100) {
                    showAlert("Diskon harus antara 0 hingga 100!");
                    return null;
                }

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Konfirmasi Diskon");
                confirm.setHeaderText("Anda akan menambahkan diskon " + diskon + "% untuk cabang: " + currentCabang.getCabangNama());
                confirm.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        applyDiskonKeCabang(diskon);
                        showAlert("Diskon berhasil diterapkan ke semua menu cabang ini.");
                    }
                });
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    private void handleHapusDiskon() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Diskon");
        confirm.setHeaderText("Apakah Anda yakin ingin menghapus diskon untuk cabang ini?");
        confirm.setContentText("Harga semua menu akan dikembalikan ke harga asli.");
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                hapusDiskonDariCabang();
                showAlert("Diskon berhasil dihapus. Semua harga kembali ke semula.");
            }
        });
    }
    private void hapusDiskonDariCabang() {
        try (Connection conn = DataSourceManager.getUserConnection()) {

            String restoreSql = """
            UPDATE menu SET menu_harga = harga_asli
            WHERE harga_asli IS NOT NULL AND cabang_id = ?
        """;
            try (PreparedStatement stmt = conn.prepareStatement(restoreSql)) {
                stmt.setInt(1, (int) currentCabang.getCabangID());
                stmt.executeUpdate();
            }

            String deleteSql = "DELETE FROM diskon WHERE cabang_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, (int) currentCabang.getCabangID());
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Gagal menghapus diskon: " + e.getMessage());
        }
    }



    private void applyDiskonKeCabang(int persen) {
        try (Connection conn = DataSourceManager.getUserConnection()) {

            int cabangId = (int) currentCabang.getCabangID();

            String insertDiskonSql = """
            INSERT INTO diskon (cabang_id, diskon_pct)
            VALUES (?, ?)
            ON CONFLICT (cabang_id) DO UPDATE SET diskon_pct = EXCLUDED.diskon_pct
        """;
            try (PreparedStatement stmt = conn.prepareStatement(insertDiskonSql)) {
                stmt.setInt(1, cabangId);
                stmt.setInt(2, persen);
                stmt.executeUpdate();
            }

            String backupSql = """
            UPDATE menu SET harga_asli = menu_harga
            WHERE harga_asli IS NULL AND cabang_id = ?
        """;
            try (PreparedStatement stmt = conn.prepareStatement(backupSql)) {
                stmt.setInt(1, cabangId);
                stmt.executeUpdate();
            }

            String applySql = """
            UPDATE menu
            SET menu_harga = harga_asli - (harga_asli * ? / 100)
            WHERE cabang_id = ?
        """;
            try (PreparedStatement stmt = conn.prepareStatement(applySql)) {
                stmt.setInt(1, persen);
                stmt.setInt(2, cabangId);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Gagal menerapkan diskon: " + e.getMessage());
        }
    }



    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void handleTampilkanPesanan() {
        if (dateFilter.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Filter Tanggal");
            alert.setHeaderText(null);
            alert.setContentText("Masukkan tanggal.");
            alert.showAndWait();
            return;
        }
        loadPesanan(dateFilter.getValue());
    }

    @FXML
    private void handleTambahAdmin() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tambah Admin Cabang");

        TextField tfNama = new TextField();
        PasswordField tfPassword = new PasswordField();
        TextField tfNotelp = new TextField();
        TextField tfEmail = new TextField();
        DatePicker dpTanggalMasuk = new DatePicker(LocalDate.now());
        TextField tfGaji = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nama:"), tfNama);
        grid.addRow(1, new Label("Password:"), tfPassword);
        grid.addRow(2, new Label("No Telp:"), tfNotelp);
        grid.addRow(3, new Label("Email:"), tfEmail);
        grid.addRow(4, new Label("Tanggal Masuk:"), dpTanggalMasuk);
        grid.addRow(5, new Label("Gaji:"), tfGaji);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String nama = tfNama.getText().trim();
                String password = tfPassword.getText().trim();
                String notelp = tfNotelp.getText().trim();
                String email = tfEmail.getText().trim();
                LocalDate tanggalMasuk = dpTanggalMasuk.getValue();
                String gajiStr = tfGaji.getText().trim();

                if (!nama.matches("[a-zA-Z\\s]+")) {
                    showAlert("Nama hanya boleh berisi huruf dan spasi.");
                    return null;
                }

                if (!notelp.matches("\\d+")) {
                    showAlert("No Telp harus berupa angka.");
                    return null;
                }

                if (!email.contains("@") || !email.contains(".")) {
                    showAlert("Email harus mengandung '@' dan '.'");
                    return null;
                }

                try {
                    double gaji = Double.parseDouble(gajiStr);
                    if (gaji <= 0) {
                        showAlert("Gaji harus lebih besar dari 0.");
                        return null;
                    }
                } catch (NumberFormatException e) {
                    showAlert("Gaji harus berupa angka.");
                    return null;
                }

                try (Connection conn = DataSourceManager.getUserConnection()) {
                    String checkQuery = "SELECT 1 FROM staff WHERE staff_nama = ?";
                    PreparedStatement ps = conn.prepareStatement(checkQuery);
                    ps.setString(1, nama);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        showAlert("Nama admin sudah digunakan. Gunakan nama lain.");
                        return null;
                    }
                } catch (SQLException e) {
                    showAlert("Gagal memeriksa keunikan nama.");
                    return null;
                }
                addNewAdmin(nama, password, notelp, email, tanggalMasuk, gajiStr);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void addNewAdmin(String nama, String password, String notelp, String email, LocalDate tanggalMasuk,
                             String gajiStr) {

        int roleId = 1;
        int cabangId = (int) currentCabang.getCabangID();

        String sql = "INSERT INTO staff (staff_nama, staff_password, notelp, staff_email, tanggal_masuk, gaji, role_id, cabang_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nama);
            stmt.setString(2, password);
            stmt.setString(3, notelp);
            stmt.setString(4, email);
            stmt.setDate(5, Date.valueOf(tanggalMasuk));
            stmt.setInt(6, Integer.parseInt(gajiStr));
            stmt.setInt(7, roleId);
            stmt.setInt(8, cabangId);

            stmt.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sukses");
            alert.setHeaderText("Admin Ditambahkan");
            alert.setContentText("Admin berhasil ditambahkan ke cabang ini.");
            alert.showAndWait();

            loadAdmin();
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Gagal");
            alert.setHeaderText("Gagal Menambahkan Admin");
            alert.setContentText("Kesalahan SQL: " + e.getMessage());
            alert.showAndWait();
        }
    }

}
