package com.example.bdsqltester.scenes.adminCabang;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Menu;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class MenuAdmincController {
    @FXML private AdminNavbarController navbarController;
    @FXML private AnchorPane navbar;
    @FXML private TableView<Menu> menuTable;
    @FXML private TableColumn<Menu, Integer> menuIdColumn;
    @FXML private TableColumn<Menu, String> menuNamaColumn;
    @FXML private TableColumn<Menu, Integer> menuHargaColumn;
    @FXML private TableColumn<Menu, Integer> menuStokColumn;
    @FXML private TextField namaField;
    @FXML private TextField hargaField;
    @FXML private TextField stokField;
    @FXML private ChoiceBox<String> kategoriDropdown;
    @FXML private Button btnStatus;

    private final ObservableList<Menu> menuList = FXCollections.observableArrayList();
    private final ObservableList<String> kategoriList = FXCollections.observableArrayList();
    private final ObservableList<Integer> kategoriIdList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/adminCabang/adminCabangNavbar.fxml"));
            AnchorPane pane = loader.load();
            navbarController = loader.getController();
            navbar.getChildren().setAll(pane);
        } catch (Exception e) { e.printStackTrace(); }

        menuIdColumn.setCellValueFactory(new PropertyValueFactory<>("menuId"));
        menuNamaColumn.setCellValueFactory(new PropertyValueFactory<>("menuNama"));
        menuHargaColumn.setCellValueFactory(new PropertyValueFactory<>("menuHarga"));
        menuStokColumn.setCellValueFactory(new PropertyValueFactory<>("menuStok"));
        menuTable.setItems(menuList);

        btnStatus.setVisible(false);
        btnStatus.setOnAction(this::toggleStatusMenu);

        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                namaField.setText(n.getMenuNama());
                hargaField.setText(String.valueOf(n.getMenuHarga()));
                stokField.setText(String.valueOf(n.getMenuStok()));
                btnStatus.setText(n.isMenuStatus() ? "Nonaktifkan" : "Aktifkan");
                btnStatus.setVisible(true);
                try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
                    String sql = "SELECT kategori_nama FROM kategori WHERE kategori_id=(SELECT kategori_id FROM menu WHERE menu_id=?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, n.getMenuId());
                    ResultSet r = ps.executeQuery();
                    if (r.next()) kategoriDropdown.setValue(r.getString("kategori_nama"));
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                clearFields();
                btnStatus.setVisible(false);
            }
        });

        loadKategoriDropdown();
        loadMenuData();
    }


    private void loadKategoriDropdown() {
        kategoriList.clear();
        kategoriIdList.clear();
        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
            String q = "SELECT kategori_id, kategori_nama FROM kategori";
            PreparedStatement ps = conn.prepareStatement(q);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                kategoriList.add(rs.getString("kategori_nama"));
                kategoriIdList.add(rs.getInt("kategori_id"));
            }
            kategoriDropdown.setItems(kategoriList);
            if (!kategoriList.isEmpty()) kategoriDropdown.setValue(kategoriList.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat kategori.");
        }
    }

    private void loadMenuData() {
        menuList.clear();
        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
            int cabangId = HelloApplication.getApplicationInstance().getCabangId();

            String q = "SELECT menu_id, menu_nama, harga_asli, stok, menu_status FROM menu WHERE cabang_id = ? ORDER BY menu_id";
            PreparedStatement ps = conn.prepareStatement(q);
            ps.setInt(1, cabangId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                menuList.add(new Menu(
                        rs.getInt("menu_id"),
                        rs.getString("menu_nama"),
                        rs.getInt("harga_asli"),
                        rs.getInt("stok"),
                        rs.getBoolean("menu_status")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data menu.");
        }
    }

    @FXML
    public void addMenu(ActionEvent evt) {
        String nama = namaField.getText().trim();
        String hargaStr = hargaField.getText().trim();
        String stokStr = stokField.getText().trim();

        if (!validateMenu(nama, hargaStr, stokStr)) return;

        int hargaAsli = Integer.parseInt(hargaStr);
        int stok = Integer.parseInt(stokStr);

        int idx = kategoriDropdown.getSelectionModel().getSelectedIndex();
        if (idx == -1) {
            showAlert(Alert.AlertType.WARNING, "Kategori", "Pilih kategori.");
            return;
        }

        int kategoriId = kategoriIdList.get(idx);
        int cabangId = HelloApplication.getApplicationInstance().getCabangId();
        int hargaDiskon = hargaAsli;

        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {

            String cekDuplikat = "SELECT 1 FROM menu WHERE cabang_id = ? AND LOWER(menu_nama) = LOWER(?)";
            try (PreparedStatement cek = conn.prepareStatement(cekDuplikat)) {
                cek.setInt(1, cabangId);
                cek.setString(2, nama);
                try (ResultSet rs = cek.executeQuery()) {
                    if (rs.next()) {
                        showAlert(Alert.AlertType.ERROR,
                                "Nama Menu Tidak Boleh Sama",
                                "Nama menu \"" + nama + "\" sudah ada di cabang ini.");
                        return;
                    }
                }
            }

            String dq = "SELECT diskon_pct FROM diskon WHERE cabang_id = ?";
            try (PreparedStatement ds = conn.prepareStatement(dq)) {
                ds.setInt(1, cabangId);
                try (ResultSet r = ds.executeQuery()) {
                    if (r.next()) {
                        int pct = r.getInt("diskon_pct");
                        hargaDiskon = hargaAsli - (hargaAsli * pct / 100);
                    }
                }
            }

            String ins = "INSERT INTO menu(menu_nama, menu_harga, harga_asli, cabang_id, kategori_id, stok, menu_status) VALUES(?,?,?,?,?,?,TRUE)";
            try (PreparedStatement ps = conn.prepareStatement(ins)) {
                ps.setString(1, nama);
                ps.setInt(2, hargaDiskon);
                ps.setInt(3, hargaAsli);
                ps.setInt(4, cabangId);
                ps.setInt(5, kategoriId);
                ps.setInt(6, stok);

                if (ps.executeUpdate() > 0) {
                    loadMenuData();
                    clearFields();
                }
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal menambah menu.");
        }
    }

    @FXML
    public void updateMenu(ActionEvent evt) {
        Menu sel = menuTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert(Alert.AlertType.WARNING, "Pilih", "Pilih menu."); return; }
        String nama = namaField.getText().trim();
        String hargaStr = hargaField.getText().trim();
        String stokStr = stokField.getText().trim();
        if(!validateMenu(nama, hargaStr, stokStr)){
            return;
        }
        try {
            int hargaAsli = Integer.parseInt(hargaStr);
            int stok = Integer.parseInt(stokStr);
            int cabangId = HelloApplication.getApplicationInstance().getCabangId();
            int hargaDiskon = hargaAsli;
            int idx = kategoriDropdown.getSelectionModel().getSelectedIndex();
            try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
                String cekDup = "SELECT 1 FROM menu " +
                        "WHERE cabang_id = ? AND LOWER(menu_nama) = LOWER(?) " +
                        "AND menu_id <> ?";
                try (PreparedStatement cek = conn.prepareStatement(cekDup)) {
                    cek.setInt(1, cabangId);
                    cek.setString(2, nama);
                    cek.setInt(3, sel.getMenuId());
                    try (ResultSet rs = cek.executeQuery()) {
                        if (rs.next()) {
                            showAlert(Alert.AlertType.ERROR,
                                    "Nama Menu Tidak Boleh Sama",
                                    "Nama menu \"" + nama + "\" sudah ada di cabang ini.");
                            return;
                        }
                    }
                }
                String dq = "SELECT diskon_pct FROM diskon WHERE cabang_id = ?";
                PreparedStatement ds = conn.prepareStatement(dq);
                ds.setInt(1, cabangId);
                ResultSet r = ds.executeQuery();
                if (r.next()) hargaDiskon = hargaAsli - hargaAsli * r.getInt("diskon_pct") / 100;

                String up = "UPDATE menu SET menu_nama=?, menu_harga=?, stok=?, kategori_id=?, harga_asli=? WHERE menu_id=?";
                PreparedStatement ps = conn.prepareStatement(up);
                ps.setString(1, nama);
                ps.setInt(2, hargaDiskon);
                ps.setInt(3, stok);
                ps.setInt(4, kategoriIdList.get(idx));
                ps.setInt(5, hargaAsli);
                ps.setInt(6, sel.getMenuId());
                if (ps.executeUpdate() > 0) { loadMenuData(); clearFields(); }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format", "Harga harus angka.");
        } catch (Exception e) { e.printStackTrace(); showAlert(Alert.AlertType.ERROR, "DB", "Gagal update."); }
    }

    @FXML
    private void toggleStatusMenu(ActionEvent e){
        Menu sel = menuTable.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        boolean aktif = sel.isMenuStatus();
        String txt = aktif ? "menonaktifkan" : "mengaktifkan";

        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Apakah anda yakin ingin " + txt + " menu ini?",
                ButtonType.YES, ButtonType.NO);
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES){
                try(Connection c = DataSourceManager.getCentralAdminConnection()){
                    PreparedStatement p = c.prepareStatement(
                            "UPDATE menu SET menu_status=? WHERE menu_id=?");
                    p.setBoolean(1, !aktif);
                    p.setInt(2, sel.getMenuId());
                    p.executeUpdate();
                    loadMenuData();
                    btnStatus.setVisible(false);
                } catch (Exception ex){ ex.printStackTrace(); }
            }
        });
    }


    private void clearFields() {
        namaField.clear(); hargaField.clear(); stokField.clear(); menuTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType t, String h, String m) {
        Alert a = new Alert(t); a.setTitle(h); a.setContentText(m); a.showAndWait();
    }

    private boolean  validateMenu(String nama, String hargaStr, String stokStr) {
        if (nama.isEmpty() || hargaStr.isEmpty() || stokStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Kosong", "Nama, Harga dan Stok harus diisi.");
            return false;
        }
        if (nama.matches(".*\\d.*")) {
            showAlert(Alert.AlertType.WARNING, "Input Salah", "Nama tidak boleh mengandung angka.");
            return false;
        }
        int hargaAsli, stok;
        try {
            hargaAsli = Integer.parseInt(hargaStr);
            stok = Integer.parseInt(stokStr);
            if (hargaAsli <= 0) {
                showAlert(Alert.AlertType.WARNING, "Input Salah", "Harga harus lebih dari 0.");
                return false;
            }
            if (stok < 0) {
                showAlert(Alert.AlertType.WARNING, "Input Salah", "Stok tidak boleh negatif.");
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Input Salah", "Harga dan Stok harus berupa angka.");
            return false;
        }
        return true;

    }
}
