package com.example.bdsqltester.scenes.user;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.OrderItem;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class orderController {

    @FXML
    private VBox menuContainer;
    @FXML
    private Button btnSubmit;
    @FXML
    private TableView<OrderItem> tableOrder;
    @FXML
    private TableColumn<OrderItem, String> colMenu;
    @FXML
    private TableColumn<OrderItem, Integer> colJumlah;
    @FXML
    private TableColumn<OrderItem, String> colHarga;
    @FXML
    private Label lblTotal;
    @FXML
    private ChoiceBox<String> kategoriChoice;
    @FXML
    private ChoiceBox<String> cabangChoice;

    private final List<MenuItem> menuList = new ArrayList<>();
    private final List<Spinner<Integer>> spinnerList = new ArrayList<>();
    ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupChoiceBoxes();
        loadMenuFromDatabase();
        setupTable();
        btnSubmit.setOnAction(event -> prosesOrder());
    }

    private void loadMenuFromDatabase() {
        menuContainer.getChildren().clear();
        menuList.clear();
        spinnerList.clear();

        String kategori = kategoriChoice.getValue();
        String cabang = cabangChoice.getValue();

        StringBuilder sql = new StringBuilder(
                "SELECT m.menu_id, m.menu_nama, m.harga_asli, m.stok " +
                        "FROM Menu m " +
                        "JOIN Kategori k ON m.kategori_id = k.kategori_id " +
                        "JOIN Cabang c ON m.cabang_id = c.cabang_id " +
                        "WHERE m.stok > 0 AND c.cabang_nama = ? AND m.menu_status = TRUE AND c.cabang_status = TRUE"
        );

        if (kategori != null && !kategori.equals("Semua Kategori")) {
            sql.append(" AND k.kategori_nama = '").append(kategori).append("'");
        }

        try (Connection conn = DataSourceManager.getUserConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            stmt.setString(1, cabang);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("menu_id");
                String nama = rs.getString("menu_nama");
                int harga = rs.getInt("harga_asli");
                int stok = rs.getInt("stok");
                MenuItem item = new MenuItem(id, nama, harga, stok);
                menuList.add(item);
                addMenuToUI(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addMenuToUI(MenuItem item) {
        Label label = new Label(item.getNama() + " (Rp " + item.getHarga() + ")");
        Spinner<Integer> spinner = new Spinner<>(0, item.getStok(), 0);
        spinner.setPrefWidth(70);
        spinnerList.add(spinner);

        HBox row = new HBox(10, label, spinner);
        menuContainer.getChildren().add(row);
    }

    private void setupTable() {
        colMenu.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMenu()));
        colJumlah.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getJumlah()).asObject());
        colHarga.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHargaFormatted()));
    }

    private void prosesOrder() {
        int totalHarga = 0;

        for (int i = 0; i < menuList.size(); i++) {
            int jumlah = spinnerList.get(i).getValue();
            if (jumlah > 0) {
                MenuItem item = menuList.get(i);


                OrderItem existingItem = null;
                for (OrderItem oi : orderItems) {
                    if (oi.getMenu().equals(item.getNama())) {
                        existingItem = oi;
                        break;
                    }
                }

                if (existingItem != null) {

                    int newJumlah = existingItem.getJumlah() + jumlah;
                    existingItem.setJumlah(newJumlah);
                } else {
                    orderItems.add(new OrderItem(item.getNama(), jumlah, item.getHarga()));
                }

                spinnerList.get(i).getValueFactory().setValue(0);
            }
        }


        for (OrderItem item : orderItems) {
            totalHarga += item.getTotal();
        }

        tableOrder.setItems(orderItems);
        tableOrder.refresh();
        lblTotal.setText("Total: Rp " + String.format("%,d", totalHarga).replace(',', '.'));
    }

    public static class MenuItem {
        private final int id;
        private final String nama;
        private final int harga;
        private final int stok;

        public MenuItem(int id, String nama, int harga, int stok) {
            this.id = id;
            this.nama = nama;
            this.harga = harga;
            this.stok = stok;
        }

        public int getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public int getHarga() {
            return harga;
        }

        public int getStok() {
            return stok;
        }
    }

    @FXML
    private void continueClick() {
        if (orderItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("You must order at least one item.");
            alert.show();
        } else {
            HelloApplication.getApplicationInstance().setCabangId(selectedCabangID());
            HelloApplication.setCurrentOrderItems((tableOrder.getItems()));
            HelloApplication.getApplicationInstance().switchScene("user/usercheckout-view.fxml", "Checkout");

        }
    }

    @FXML
    private void onHistoryClick() {
        HelloApplication.getApplicationInstance().switchScene("user/history-view.fxml", "History View");
    }

    private void setupChoiceBoxes() {
        kategoriChoice.getItems().clear();
        cabangChoice.getItems().clear();

        kategoriChoice.getItems().add("Semua Kategori");

        try (Connection conn = DataSourceManager.getUserConnection();
             Statement stmt = conn.createStatement()) {


            ResultSet rsKategori = stmt.executeQuery("SELECT kategori_nama FROM kategori");
            while (rsKategori.next()) {
                String kategori = rsKategori.getString("kategori_nama");
                if (kategori != null) {
                    kategoriChoice.getItems().add(kategori);
                }
            }

            ResultSet rsCabang = stmt.executeQuery("SELECT cabang_nama FROM cabang WHERE cabang_status = TRUE");
            while (rsCabang.next()) {
                String cabang = rsCabang.getString("cabang_nama");
                if (cabang != null) {
                    cabangChoice.getItems().add(cabang);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        kategoriChoice.setValue("Semua Kategori");
        cabangChoice.setValue("Pilih Cabang");

        kategoriChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadMenuFromDatabase());
        cabangChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadMenuFromDatabase());
    }

    private int selectedCabangID() {
        String cabang = cabangChoice.getValue();
        try (Connection c = DataSourceManager.getUserConnection()) {
            PreparedStatement pstmt = c.prepareStatement("SELECT cabang_id FROM cabang WHERE cabang_nama = ?");
            pstmt.setString(1, cabang);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("cabang_id");
                return id;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

}
