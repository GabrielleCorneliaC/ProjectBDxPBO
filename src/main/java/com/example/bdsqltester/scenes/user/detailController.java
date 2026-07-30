package com.example.bdsqltester.scenes.user;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class detailController implements Initializable {

    @FXML
    private Label labelNoTransaksi;
    @FXML
    private Label labelTanggal;
    @FXML
    private Label labelJam;
    @FXML
    private Label labelMetode;
    @FXML
    private Label labelSubtotal;
    @FXML
    private Label labelDiskon;
    @FXML
    private Label labelTotal;
    @FXML
    private VBox orderListContainer;
    @FXML
    private Label labelPengiriman;


    private int transaksiId = -1;

    public void setTransaksiId(int id) {
        this.transaksiId = id;
        loadTransaksiData();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    private void loadTransaksiData() {
        if (transaksiId <= 0) {
            showError("Transaksi ID belum diset!");
            return;
        }

        try (Connection conn = DataSourceManager.getUserConnection()) {

            String q1 = """
                        SELECT t.transaksi_id, t.transaksi_tanggal, t.transaksi_jam,
                               t.total_harga, COALESCE(d.diskon_pct, 0) AS diskon_pct,
                               m.metode_jenis, p.pengiriman_jenis
                        FROM transaksi t
                        LEFT JOIN diskon d ON t.cabang_id = d.cabang_id
                        LEFT JOIN metodepembayaran m ON t.metode_id = m.metode_id
                        LEFT JOIN pengiriman p ON t.pengiriman_id = p.pengiriman_id
                        WHERE t.transaksi_id = ?
                    """;

            PreparedStatement ps1 = conn.prepareStatement(q1);
            ps1.setInt(1, transaksiId);
            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {

                labelNoTransaksi.setText("No. Pesanan: O-" + transaksiId);
                Date tanggal = rs1.getDate("transaksi_tanggal");
                Time jam = rs1.getTime("transaksi_jam");

                if (tanggal != null)
                    labelTanggal.setText(new SimpleDateFormat("dd/MM/yyyy").format(tanggal));
                if (jam != null)
                    labelJam.setText(new SimpleDateFormat("HH:mm").format(jam));

                labelMetode.setText(rs1.getString("metode_jenis"));

                String jenisPengiriman = rs1.getString("pengiriman_jenis");
                if (jenisPengiriman != null) {
                    labelPengiriman.setText(jenisPengiriman);
                } else {
                    labelPengiriman.setText("-");
                }

                int subtotal = getSubtotalPesanan(conn, transaksiId);

                int diskonPct = rs1.getObject("diskon_pct") != null ? rs1.getInt("diskon_pct") : 0;

                int diskon = (subtotal * diskonPct) / 100;

                int total = subtotal - diskon;

                labelSubtotal.setText("Rp " + subtotal);
                labelDiskon.setText("-" + diskonPct + "% (Rp " + diskon + ")");
                labelTotal.setText("Rp " + total);

            } else {
                showError("Transaksi tidak ditemukan.");
                return;
            }

            String q2 = """
                    SELECT m.menu_nama, p.jumlah, (p.jumlah * m.harga_asli) AS total_harga
                    FROM pesanan p
                    JOIN menu m ON p.menu_id = m.menu_id
                    WHERE p.transaksi_id = ?
                    """;

            PreparedStatement ps2 = conn.prepareStatement(q2);
            ps2.setInt(1, transaksiId);
            ResultSet rs2 = ps2.executeQuery();

            orderListContainer.getChildren().clear();
            while (rs2.next()) {
                String nama = rs2.getString("menu_nama");
                int jumlah = rs2.getInt("jumlah");
                int totalHarga = rs2.getInt("total_harga");

                Label item = new Label(nama + " x" + jumlah + " - Rp " + totalHarga);
                item.setStyle("-fx-font-size: 13px;");
                orderListContainer.getChildren().add(item);
            }

        } catch (SQLException e) {
            showError("Kesalahan database: " + e.getMessage());
        }
    }

    private int getSubtotalPesanan(Connection conn, int transaksiId) throws SQLException {
        String q = """
                SELECT SUM(p.jumlah * m.harga_asli) AS subtotal
                FROM pesanan p
                JOIN menu m ON p.menu_id = m.menu_id
                WHERE p.transaksi_id = ?
                """;

        PreparedStatement ps = conn.prepareStatement(q);
        ps.setInt(1, transaksiId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("subtotal");
        }
        return 0;
    }

    @FXML
    private void onKembaliClick(ActionEvent event) {
        HelloApplication.getApplicationInstance().switchScene("user/order-view.fxml", "Order View");
    }

    @FXML
    private void onRiwayatClick(ActionEvent event) {
        HelloApplication.getApplicationInstance().switchScene("user/history-view.fxml", "History View");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
