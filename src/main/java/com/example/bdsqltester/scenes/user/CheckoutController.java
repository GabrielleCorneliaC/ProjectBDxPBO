package com.example.bdsqltester.scenes.user;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.OrderItem;
import com.example.bdsqltester.dtos.JadwalItem;
import com.example.bdsqltester.dtos.MetodeItem;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class CheckoutController {

    private ObservableList<OrderItem> orderItems = HelloApplication.getCurrentOrderItems();

    @FXML
    private TableView<OrderItem> orderTable;
    @FXML
    private TableColumn<OrderItem, String> itemNameColumn;
    @FXML
    private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML
    private TableColumn<OrderItem, String> priceColumn;

    @FXML
    private Label totalPriceLabel;
    @FXML
    private Label diskonLabel;
    @FXML
    private Label totalPembayaranLabel;


    @FXML
    private ComboBox<JadwalItem> comboJadwal;
    @FXML
    private ComboBox<MetodeItem> comboMetodePembayaran;
    @FXML
    private ComboBox<String> comboKirim;

    private final ObservableList<JadwalItem> jadwalList = FXCollections.observableArrayList();
    private final ObservableList<MetodeItem> metodeList = FXCollections.observableArrayList();
    private final ObservableList<String> kirimList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadOrder();
        loadMetodePembayaran();
        loadPengiriman();
        comboKirim.setOnAction(e -> {
            String selectedJenis = comboKirim.getValue();
            if (selectedJenis != null) {
                loadJadwal(selectedJenis);
            }
        });

    }

    private int total, nominalDiskon, totalBayar;

    private void loadPengiriman() {
        String sql = "SELECT pengiriman_id, pengiriman_jenis FROM pengiriman";

        try (Connection conn = DataSourceManager.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                kirimList.add(rs.getString("pengiriman_jenis"));
            }
            comboKirim.setItems(kirimList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadJadwal(String jenisPengiriman) {
        String sql = """
                    SELECT j.jadwal_id, j.jadwal_tanggal, j.jadwal_jamkirim, j.jadwal_jamsampai 
                    FROM JadwalKirim j
                    JOIN pengiriman p ON j.pengiriman_id = p.pengiriman_id
                    WHERE j.status_jadwal = 'available' AND j.cabang_id = ? AND p.pengiriman_jenis = ?
                """;

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        jadwalList.clear();

        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, HelloApplication.getApplicationInstance().getCabangId());
            ps.setString(2, jenisPengiriman);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("jadwal_id");
                    Date tanggal = rs.getDate("jadwal_tanggal");
                    Time jamKirim = rs.getTime("jadwal_jamkirim");
                    Time jamSampai = rs.getTime("jadwal_jamsampai");

                    String label = tanggal + " (" +
                            jamKirim.toLocalTime().format(timeFormat) + " - " +
                            jamSampai.toLocalTime().format(timeFormat) + ")";

                    jadwalList.add(new JadwalItem(id, label));
                }
            }
            comboJadwal.setItems(jadwalList);

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText(null);
            alert.setContentText("Gagal memuat jadwal pengiriman:\n" + e.getMessage());
            alert.showAndWait();
        }
    }


    private void loadMetodePembayaran() {
        String sql = "SELECT metode_id, metode_jenis FROM MetodePembayaran";

        try (Connection conn = DataSourceManager.getUserConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                metodeList.add(new MetodeItem(rs.getInt("metode_id"), rs.getString("metode_jenis")));
            }
            comboMetodePembayaran.setItems(metodeList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadOrder() {
        itemNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMenu()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getJumlah()).asObject());
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHargaFormatted()));
        orderTable.setItems(orderItems);

        total = orderItems.stream()
                .mapToInt(OrderItem::getTotal)
                .sum();

        totalPriceLabel.setText("Total: Rp " + String.format("%,d", total).replace(',', '.'));

        nominalDiskon = total * getDiskonPctFromDB() / 100;
        totalBayar = total - nominalDiskon;

        diskonLabel.setText("Diskon: " + getDiskonPctFromDB() + " %");
        totalPembayaranLabel.setText("Total Pembayaran: Rp " +
                String.format("%,d", totalBayar).replace(',', '.'));
    }

    private int getDiskonPctFromDB() {
        String sql = "SELECT diskon_pct FROM diskon WHERE cabang_id = ?";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, HelloApplication.getApplicationInstance().getCabangId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("diskon_pct");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @FXML
    private void confirmOrder() {
        if (comboMetodePembayaran.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Harap pilih metode pembayaran.").show();
            return;
        }

        if (comboJadwal.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Harap pilih jadwal pengiriman.").show();
            return;
        }

        if (comboKirim.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Harap pilih metode pengiriman.").show();
            return;
        }

        String metode = comboMetodePembayaran.getValue().getLabel().toLowerCase();
        String pesan = "";
        switch (metode) {
            case "cash":
                pesan = "Berikan uang sejumlah total kepada driver.";
                break;
            case "debit":
            case "kredit":
                pesan = "Driver akan membawa alat pembayaran.";
                break;
            case "qris":
                pesan = "Silakan scan barcode QRIS yang ada pada driver.";
                break;
            default:
                pesan = "";
        }

        if (!pesan.isEmpty()) {
            Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
            alertInfo.setTitle("Informasi Metode Pembayaran");
            alertInfo.setHeaderText(null);
            alertInfo.setContentText(pesan);
            alertInfo.showAndWait();
        }

        try (Connection conn = DataSourceManager.getUserConnection()) {
            conn.setAutoCommit(false);

            int cabangId = HelloApplication.getApplicationInstance().getCabangId();

            int pengirimanId = getPengirimanIdByJenis(comboKirim.getValue());

            String insertTrans = "INSERT INTO Transaksi (total_harga, metode_id, transaksi_tanggal, pelanggan_id, transaksi_status, cabang_id, total_pembayaran, pengiriman_id) " +
                    "VALUES (?, ?, CURRENT_DATE, ?, 'Diproses', ?, ?, ?) RETURNING transaksi_id";

            PreparedStatement transStmt = conn.prepareStatement(insertTrans);
            transStmt.setInt(1, total);
            transStmt.setInt(2, comboMetodePembayaran.getValue().getId());
            transStmt.setInt(3, HelloApplication.getApplicationInstance().getUserId());
            transStmt.setInt(4, cabangId);
            transStmt.setInt(5, totalBayar);
            transStmt.setInt(6, pengirimanId);


            ResultSet transRs = transStmt.executeQuery();
            int transaksiId = -1;
            if (transRs.next()) {
                transaksiId = transRs.getInt("transaksi_id");
            }

            String getMenuIdSQL = "SELECT menu_id FROM Menu WHERE menu_nama = ?";
            String insertPesananSQL = "INSERT INTO Pesanan (menu_id, transaksi_id, jumlah) VALUES (?, ?, ?)";
            PreparedStatement menuIdStmt = conn.prepareStatement(getMenuIdSQL);
            PreparedStatement pesananStmt = conn.prepareStatement(insertPesananSQL);

            for (OrderItem item : orderItems) {
                menuIdStmt.setString(1, item.getMenu());
                ResultSet rs = menuIdStmt.executeQuery();
                if (rs.next()) {
                    int menuId = rs.getInt("menu_id");
                    pesananStmt.setInt(1, menuId);
                    pesananStmt.setInt(2, transaksiId);
                    pesananStmt.setInt(3, item.getJumlah());
                    pesananStmt.executeUpdate();

                    String updateStockSQL = "UPDATE Menu SET stok = stok - ? WHERE menu_id = ?";
                    PreparedStatement stockStmt = conn.prepareStatement(updateStockSQL);
                    stockStmt.setInt(1, item.getJumlah());
                    stockStmt.setInt(2, menuId);
                    stockStmt.executeUpdate();
                }
            }

            String updateJadwal = "UPDATE JadwalKirim SET status_jadwal = 'unavailable', transaksi_id = ? WHERE jadwal_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateJadwal);
            updateStmt.setInt(1, transaksiId);
            updateStmt.setInt(2, comboJadwal.getValue().getId());
            updateStmt.executeUpdate();

            conn.commit();

            new Alert(Alert.AlertType.INFORMATION, "Pesanan berhasil dibuat!").show();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/user/detail-view.fxml"));
            Parent root = loader.load();

            detailController controller = loader.getController();
            controller.setTransaksiId(transaksiId);

            Stage stage = HelloApplication.getApplicationInstance().getPrimaryStage();
            stage.setScene(new Scene(root));
            stage.setTitle("Order Detail");
            stage.show();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal membuat pesanan.").show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getPengirimanIdByJenis(String jenis) {
        String sql = "SELECT pengiriman_id FROM pengiriman WHERE pengiriman_jenis = ?";
        try (Connection conn = DataSourceManager.getUserConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jenis);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("pengiriman_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void cancelOrder(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Pembatalan");
        alert.setHeaderText("Apakah Anda ingin membatalkan pesanan?");
        alert.setContentText("Jika Anda membatalkan, pesanan Anda akan dibatalkan.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/bdsqltester/user/order-view.fxml"));

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    Scene scene = new Scene(loader.load());
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}

