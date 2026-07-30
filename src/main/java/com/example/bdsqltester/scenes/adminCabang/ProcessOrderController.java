package com.example.bdsqltester.scenes.adminCabang;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Transaksi;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.sql.*;

public class ProcessOrderController {

    @FXML
    private AdminNavbarController navbarController;
    @FXML
    private AnchorPane navbar;
    @FXML
    private TableView<Transaksi> transaksiTable;
    @FXML
    private TableColumn<Transaksi, Integer> idColumn;
    @FXML
    private TableColumn<Transaksi, Date> tanggalColumn;
    @FXML
    private TableColumn<Transaksi, Time> jamColumn;
    @FXML
    private TableColumn<Transaksi, Integer> totalColumn;
    @FXML
    private TableColumn<Transaksi, String> statusColumn;
    @FXML
    private TableColumn<Transaksi, String> pelangganColumn;

    @FXML
    private Button reviewButton;

    @FXML
    private Button selesaiButton;

    @FXML
    private Button detailButton;

    @FXML
    private void initialize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/adminCabang/adminCabangNavbar.fxml"));
            AnchorPane pane = loader.load();
            navbarController = loader.getController();
            navbar.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }

        idColumn.setCellValueFactory(new PropertyValueFactory<>("transaksiId"));
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        jamColumn.setCellValueFactory(new PropertyValueFactory<>("jam"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        pelangganColumn.setCellValueFactory(new PropertyValueFactory<>("pelanggan"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTransaction();

        transaksiTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean selected = newSel != null;
            detailButton.setDisable(!selected);
            selesaiButton.setDisable(!selected || ("Selesai".equalsIgnoreCase(newSel.getStatus())));
            reviewButton.setDisable(!selected || !checkReview(newSel));
        });
    }


    private void loadTransaction() {
        transaksiTable.getItems().clear();

        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
            int cabangId = HelloApplication.getApplicationInstance().getCabangId();

            String query = """
                    SELECT t.transaksi_id, t.transaksi_tanggal, t.transaksi_jam, t.total_pembayaran, 
                           p.pelanggan_nama, t.transaksi_status
                    FROM transaksi t
                    JOIN pelanggan p ON t.pelanggan_id = p.pelanggan_id
                    WHERE t.cabang_id = ?
                    ORDER BY t.transaksi_id
                    """;

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, cabangId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaksi trx = new Transaksi(
                        rs.getInt("transaksi_id"),
                        rs.getDate("transaksi_tanggal"),
                        rs.getTime("transaksi_jam"),
                        rs.getInt("total_pembayaran"),
                        rs.getString("pelanggan_nama"),
                        rs.getString("transaksi_status")
                );
                transaksiTable.getItems().add(trx);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data menu.");
            e.printStackTrace();
        }
    }

    private boolean checkReview(Transaksi t) {
        return t.getStatus().equalsIgnoreCase("Selesai");
    }

    @FXML
    private void reviewClick() {
        Transaksi trans = transaksiTable.getSelectionModel().getSelectedItem();
        if (trans == null) return;

        String sql = """
                SELECT rating, review
                FROM penilaian
                WHERE transaksi_id = ?
                """;

        String isi = "Belum ada review.";
        String header = "Belum ada rating";
        try (Connection conn = DataSourceManager.getCentralAdminConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, trans.getTransaksiId());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                isi = "Review: " + rs.getString("review");
                header = "Rating  : " + rs.getInt("rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            isi = "Gagal memuat review.";
        }

        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Review Transaksi");
        dlg.setHeaderText(header);
        dlg.setContentText(isi);
        dlg.showAndWait();
    }


    @FXML
    private void selesaiClick() {
        Transaksi trans = transaksiTable.getSelectionModel().getSelectedItem();
        if (trans == null) return;

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Ubah status transaksi menjadi Selesai?",
                ButtonType.OK, ButtonType.CANCEL);
        conf.setTitle("Konfirmasi Selesai");
        conf.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String sql = "UPDATE transaksi SET transaksi_status = 'Selesai' WHERE transaksi_id = ?";
                try (Connection conn = DataSourceManager.getCentralAdminConnection();
                     PreparedStatement st = conn.prepareStatement(sql)) {
                    st.setInt(1, trans.getTransaksiId());
                    st.executeUpdate();
                    loadTransaction();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal mengubah status transaksi.");
                    e.printStackTrace();
                }
            }
        });
    }


    @FXML
    private void detailClick() {
        Transaksi trx = transaksiTable.getSelectionModel().getSelectedItem();
        if (trx == null) return;

        String sql = """
                SELECT m.menu_nama, ps.jumlah, m.harga_asli, t.total_pembayaran
                FROM pesanan ps
                JOIN menu m ON ps.menu_id = m.menu_id
                JOIN transaksi t ON t.transaksi_id = ps.transaksi_id
                WHERE ps.transaksi_id = ?
                """;


        StringBuilder detail = new StringBuilder();
        StringBuilder head = new StringBuilder();
        StringBuilder alamat = new StringBuilder();
        int total = 0, bayar = 0;
        try (Connection conn = DataSourceManager.getCentralAdminConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, trx.getTransaksiId());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String menu = rs.getString("menu_nama");
                int jumlah = rs.getInt("jumlah");
                int harga = rs.getInt("harga_asli");
                bayar = rs.getInt("total_pembayaran");
                detail.append(menu)
                        .append(" Rp")
                        .append(harga)
                        .append("  x ")
                        .append(jumlah)
                        .append("\n");

                total += harga * jumlah;


            }
            detail.append("Total Harga: Rp")
                    .append(total)
                    .append("\n")
                    .append("Total Pembayaran (Setelah Diskon): Rp")
                    .append(bayar);

        } catch (SQLException e) {
            e.printStackTrace();
            detail.append("Gagal memuat detail.");
        }

        sql = """
                SELECT * FROM alamat
                WHERE pelanggan_id = (SELECT pelanggan_id from transaksi
                                WHERE transaksi_id = ?)
                """;
        try (Connection conn = DataSourceManager.getCentralAdminConnection()) {
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, trx.getTransaksiId());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String jalan = rs.getString("alamat_jalan");
                String kota = rs.getString("alamat_kota");
                String provinsi = rs.getString("alamat_provinsi");
                String kode = rs.getString("kode_pos");
                alamat.append(jalan).append(", ").append(kota).append(", ").append(provinsi).append(" ").append(kode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            detail.append("Gagal memuat alamat.");

        }
        head.append("Transaksi ID: ")
                .append(trx.getTransaksiId())
                .append("\n")
                .append("Alamat: ")
                .append(alamat);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detail Pesanan");
        alert.setHeaderText(head.toString());
        alert.setContentText(detail.toString());
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
