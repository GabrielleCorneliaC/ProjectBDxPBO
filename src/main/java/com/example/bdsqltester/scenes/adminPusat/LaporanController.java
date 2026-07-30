package com.example.bdsqltester.scenes.adminPusat;

import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Cabang;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class LaporanController {

    @FXML
    private Label labelNamaCabang;
    @FXML
    private Label labelAlamatCabang;
    @FXML
    private Label labelTotalTransaksi;
    @FXML
    private Label labelMenuTerlaris;
    @FXML
    private Label labelDiskon;
    @FXML
    private Label labelRating;
    @FXML
    private Label labelAdminTerbaru;

    private Cabang currentCabang;

    public void setCabang(Cabang cabang) {
        this.currentCabang = cabang;

        labelNamaCabang.setText(cabang.getCabangNama());
        labelAlamatCabang.setText(cabang.getCabangAlamat());

        tampilkanTotalTransaksiHariIni();
        tampilkanMenuTerlaris();
        tampilkanDiskon();
        tampilkanRatingRataRata();
        tampilkanAdminTerbaru();
    }

    private void tampilkanDiskon() {
        String sql = "SELECT COALESCE(diskon_pct, 0) AS persen FROM diskon WHERE cabang_id = ?";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, currentCabang.getCabangID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                labelDiskon.setText(rs.getInt("persen") + "%");
            } else {
                labelDiskon.setText("0%");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            labelDiskon.setText("0%");
        }
    }

    private void tampilkanTotalTransaksiHariIni() {
        String sql = "SELECT COUNT(*) FROM transaksi WHERE cabang_id = ? AND transaksi_tanggal = ?";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, currentCabang.getCabangID());
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                labelTotalTransaksi.setText(rs.getInt(1) + " transaksi");
            } else {
                labelTotalTransaksi.setText("0 transaksi");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanMenuTerlaris() {
        String sql = """
                    SELECT m.menu_nama,
                           SUM(p.jumlah) AS total_jumlah
                    FROM  pesanan   p
                    JOIN  menu      m ON p.menu_id      = m.menu_id
                    JOIN  transaksi t ON p.transaksi_id = t.transaksi_id
                    WHERE t.cabang_id = ?               -- cabang yang sedang dibuka
                    GROUP BY m.menu_nama
                    ORDER BY total_jumlah DESC
                    LIMIT 1
                """;

        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, currentCabang.getCabangID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                labelMenuTerlaris.setText(rs.getString("menu_nama") + " (" + rs.getInt("total_jumlah") + "x)");
            } else labelMenuTerlaris.setText("Belum ada");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanRatingRataRata() {
        String sql = """ 
                SELECT AVG(p.rating) AS avg_rating
                FROM penilaian p
                JOIN transaksi t ON p.transaksi_id = t.transaksi_id
                WHERE t.cabang_id = ?
                """;
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, currentCabang.getCabangID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg_rating");
                labelRating.setText(String.format("%.2f", avg));
            } else {
                labelRating.setText("-");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tampilkanAdminTerbaru() {
        String sql = """
                SELECT staff_nama, tanggal_masuk
                FROM staff
                WHERE cabang_id = ?
                ORDER BY tanggal_masuk DESC
                LIMIT 1
                """;
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, currentCabang.getCabangID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                labelAdminTerbaru.setText(rs.getString("staff_nama") + " (" + rs.getDate("tanggal_masuk") + ")");
            } else {
                labelAdminTerbaru.setText("Belum ada");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
