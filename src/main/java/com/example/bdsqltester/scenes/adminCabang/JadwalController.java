package com.example.bdsqltester.scenes.adminCabang;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Jadwal;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class JadwalController {
    @FXML
    private AdminNavbarController navbarController;
    @FXML
    private AnchorPane navbar;

    @FXML private TableView<Jadwal> jadwalTable;
    @FXML private TableColumn<Jadwal, Integer> jadwalNoColumn;
    @FXML private TableColumn<Jadwal, LocalDate> jadwalTglColumn;
    @FXML private TableColumn<Jadwal, Timestamp> jamkirimColumn;
    @FXML private TableColumn<Jadwal, Timestamp> jamsampaiColumn;

    @FXML private DatePicker tanggal;
    @FXML private TextField jamkirimField;
    @FXML private TextField jamselesaiField;
    @FXML private ComboBox<String> pengirimanCombo;


    @FXML private Button updateBtn;
    @FXML private Button deleteBtn;

    private final ObservableList<Jadwal> jadwalList = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/adminCabang/adminCabangNavbar.fxml"));
            AnchorPane pane = loader.load();
            navbarController = loader.getController();
            navbar.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }

        jadwalNoColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalId"));
        jadwalTglColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalTanggal"));
        jamkirimColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalJamkirim"));
        jamsampaiColumn.setCellValueFactory(new PropertyValueFactory<>("jadwalJamsampai"));
        jadwalTable.setItems(jadwalList);

        loadJadwalData();

        jadwalTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disable = newVal == null;
            updateBtn.setDisable(disable);
            deleteBtn.setDisable(disable);

            if (newVal != null) {
                tanggal.setValue(newVal.getJadwalTanggal());
                jamkirimField.setText(newVal.getJadwalJamkirim().toLocalDateTime().toLocalTime().toString());
                jamselesaiField.setText(newVal.getJadwalJamsampai().toLocalDateTime().toLocalTime().toString());
            }
        });

        updateBtn.setDisable(true);
        deleteBtn.setDisable(true);
        pengirimanCombo.setItems(FXCollections.observableArrayList("Ekonomis", "Standard", "Express"));

    }

    private void loadJadwalData() {
        jadwalList.clear();
        String sql = "SELECT jk.jadwal_id, jk.jadwal_tanggal, jk.jadwal_jamkirim, jk.jadwal_jamsampai, " +
                "jk.pengiriman_id, p.pengiriman_jenis " +
                "FROM jadwalkirim jk " +
                "JOIN pengiriman p ON jk.pengiriman_id = p.pengiriman_id " +
                "WHERE jk.cabang_id = ? " +
                "ORDER BY jk.jadwal_id";
        try (Connection conn = DataSourceManager.getCentralAdminConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, HelloApplication.getApplicationInstance().getCabangId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Jadwal jadwal = new Jadwal(
                            rs.getInt("jadwal_id"),
                            rs.getDate("jadwal_tanggal").toLocalDate(),
                            rs.getTimestamp("jadwal_jamkirim"),
                            rs.getTimestamp("jadwal_jamsampai"),
                            rs.getInt("pengiriman_id"),
                            rs.getString("pengiriman_jenis")
                    );
                    jadwalList.add(jadwal);
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memuat data jadwal: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    "Gagal memuat data jadwal.");
        }
    }


    @FXML
    public void addJadwal(ActionEvent event) {
        processJadwalData("add", null);
    }

    @FXML
    public void updateJadwal(ActionEvent event) {
        Jadwal selected = jadwalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Jadwal", "Silakan pilih jadwal yang ingin diubah.");
            return;
        }
        processJadwalData("update", selected);
    }

    private void processJadwalData(String action, Jadwal selected) {
        LocalDate tgl = tanggal.getValue();
        String jamKirimText = jamkirimField.getText();
        String jamSampaiText = jamselesaiField.getText();

        if (tgl == null || jamKirimText.isEmpty() || jamSampaiText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Kosong", "Isi semua input tanggal dan jam terlebih dahulu.");
            return;
        }

        LocalTime timeKirim, timeSampai;
        try {
            timeKirim = LocalTime.parse(jamKirimText, timeFormatter);
            timeSampai = LocalTime.parse(jamSampaiText, timeFormatter);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Format Jam Salah", "Gunakan format HH:mm:ss untuk jam.");
            return;
        }

        if (timeKirim.compareTo(timeSampai) >= 0) {
            showAlert(Alert.AlertType.WARNING, "Jam Tidak Valid", "Jam sampai harus setelah jam kirim.");
            return;
        }

        Timestamp tsKirim = Timestamp.valueOf(LocalDateTime.of(tgl, timeKirim));
        Timestamp tsSampai = Timestamp.valueOf(LocalDateTime.of(tgl, timeSampai));

        String jenisPengiriman = pengirimanCombo.getValue();
        if (jenisPengiriman == null) {
            showAlert(Alert.AlertType.WARNING, "Jenis Pengiriman", "Silakan pilih jenis pengiriman.");
            return;
        }

        int pengirimanId = getPengirimanIdByJenis(jenisPengiriman);
        if (pengirimanId == -1) {
            showAlert(Alert.AlertType.ERROR, "Data Error", "Jenis pengiriman tidak ditemukan.");
            return;
        }

        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
            if ("add".equals(action)) {
                String insert = "INSERT INTO JadwalKirim (jadwal_tanggal, jadwal_jamkirim, jadwal_jamsampai, cabang_id, pengiriman_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setDate(1, java.sql.Date.valueOf(tgl));
                    ps.setTimestamp(2, tsKirim);
                    ps.setTimestamp(3, tsSampai);
                    ps.setInt(4, HelloApplication.getApplicationInstance().getCabangId());
                    ps.setInt(5, pengirimanId);
                    ps.executeUpdate();
                }
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil ditambahkan.");
            } else if ("update".equals(action)) {
                String update = "UPDATE JadwalKirim SET jadwal_tanggal = ?, jadwal_jamkirim = ?, jadwal_jamsampai = ?, pengiriman_id = ? WHERE jadwal_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setDate(1, java.sql.Date.valueOf(tgl));
                    ps.setTimestamp(2, tsKirim);
                    ps.setTimestamp(3, tsSampai);
                    ps.setInt(4, pengirimanId);
                    ps.setInt(5, selected.getJadwalId());
                    ps.executeUpdate();
                }
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil diperbarui.");
            }

            loadJadwalData();
            clearFields();

        } catch (Exception e) {
            System.err.println("Error menyimpan jadwal: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Kesalahan", "Terjadi kesalahan saat menyimpan data.");
        }
    }


    @FXML
    public void deleteJadwal(ActionEvent event) {
        Jadwal selected = jadwalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Pilih Jadwal", "Silakan pilih jadwal yang ingin dihapus.");
            return;
        }

        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
            String delete = "DELETE FROM JadwalKirim WHERE jadwal_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delete)) {
                ps.setInt(1, selected.getJadwalId());
                ps.executeUpdate();
            }
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Jadwal berhasil dihapus.");
            loadJadwalData();
            clearFields();
        } catch (Exception e) {
            System.err.println("Gagal menghapus jadwal: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menghapus data.");
        }
    }

    private void clearFields() {
        tanggal.setValue(null);
        jamkirimField.clear();
        jamselesaiField.clear();
        jadwalTable.getSelectionModel().clearSelection();
    }
    private int getPengirimanIdByJenis(String jenis) {
        String sql = "SELECT pengiriman_id FROM Pengiriman WHERE LOWER(pengiriman_jenis) = LOWER(?)";
        try (Connection conn = DataSourceManager.getCentralAdminConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jenis.toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("pengiriman_id");
        } catch (SQLException e) {
            System.err.println("Gagal ambil pengiriman_id: " + e.getMessage());
        }
        return -1;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
