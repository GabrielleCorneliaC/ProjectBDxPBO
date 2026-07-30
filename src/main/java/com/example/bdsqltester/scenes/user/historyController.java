package com.example.bdsqltester.scenes.user;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.DataSourceManager;
import com.example.bdsqltester.dtos.Transaksi;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;


import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Optional;

public class historyController {
    @FXML
    private TableView<Transaksi> tableHistori;
    @FXML
    private TableColumn<Transaksi, Integer> colHistoriID;
    @FXML
    private TableColumn<Transaksi, String> colHistoriTanggal;
    @FXML
    private TableColumn<Transaksi, String> colHistoriJam;
    @FXML
    private TableColumn<Transaksi, Integer> colHistoriTotal;
    @FXML
    private TableColumn<Transaksi, String> colHistoriStatus;
    @FXML
    private TableColumn<Transaksi, String> colHistoriRating;
    @FXML
    private TableColumn<Transaksi, String> colHistoriReview;

    private final ObservableList<Transaksi> transaksiList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colHistoriID.setCellValueFactory(new PropertyValueFactory<>("transaksiId"));
        colHistoriTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colHistoriJam.setCellValueFactory(new PropertyValueFactory<>("jam"));
        colHistoriTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colHistoriStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colHistoriRating.setCellValueFactory(new PropertyValueFactory<>("ratingString"));
        colHistoriReview.setCellValueFactory(new PropertyValueFactory<>("review"));

        loadHistori();
        addActionColumn();
    }

    private void loadHistori() {
        transaksiList.clear();
        try (Connection conn = DataSourceManager.getUserConnection()) {
            int pelangganId = HelloApplication.getApplicationInstance().getUserId();
            String query = """
                        SELECT t.transaksi_id, t.transaksi_tanggal, t.transaksi_jam, t.total_pembayaran, t.transaksi_status,
                               COALESCE(p.rating || '⭐', 'Belum') AS rating,
                               COALESCE(p.review, '-') AS review
                        FROM transaksi t
                        LEFT JOIN penilaian p ON t.transaksi_id = p.transaksi_id
                        WHERE t.pelanggan_id = ?
                        ORDER BY t.transaksi_tanggal DESC, t.transaksi_jam DESC
                    """;

            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, pelangganId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Transaksi trx = new Transaksi(
                        rs.getInt("transaksi_id"),
                        rs.getDate("transaksi_tanggal"),
                        rs.getTime("transaksi_jam"),
                        rs.getInt("total_pembayaran"),
                        null,
                        rs.getString("transaksi_status")
                );
                trx.setRatingString(rs.getString("rating"));
                trx.setReview(rs.getString("review"));
                transaksiList.add(trx);
            }
            tableHistori.setItems(transaksiList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addActionColumn() {
        TableColumn<Transaksi, Void> colAction = new TableColumn<>("Aksi");
        colAction.setPrefWidth(120);

        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button rateButton = new Button("★ Beri Rating");

            {
                rateButton.setStyle("-fx-background-color: #FF9F1C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;");
                rateButton.setOnMouseEntered(e -> rateButton.setStyle("-fx-background-color: #FFBF69; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"));
                rateButton.setOnMouseExited(e -> rateButton.setStyle("-fx-background-color: #FF9F1C; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-cursor: hand;"));

                rateButton.setOnAction(event -> {
                    Transaksi trx = getTableView().getItems().get(getIndex());
                    tampilkanDialogRating(trx);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Transaksi trx = getTableView().getItems().get(getIndex());
                    if ("Selesai".equalsIgnoreCase(trx.getStatus()) && belumDiberiRating(trx.getTransaksiId())) {
                        setGraphic(rateButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        tableHistori.getColumns().add(colAction);
    }


    private boolean belumDiberiRating(int transaksiId) {
        try (Connection conn = DataSourceManager.getUserConnection()) {
            String query = "SELECT COUNT(*) as jumlah FROM penilaian WHERE transaksi_id = ? AND pelanggan_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, transaksiId);
            ps.setInt(2, HelloApplication.getApplicationInstance().getUserId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("jumlah") == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void tampilkanDialogRating(Transaksi trx) {
        Dialog<Pair<Integer, String>> dialog = new Dialog<>();
        dialog.setTitle("Beri Rating");
        dialog.setHeaderText("Beri rating dan ulasan untuk transaksi ID: " + trx.getTransaksiId());

        Label ratingLabel = new Label("Rating (0-5): ");
        Spinner<Integer> ratingSpinner = new Spinner<>(0, 5, 5);
        Label reviewLabel = new Label("Review: ");
        TextField reviewField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(ratingLabel, 0, 0);
        grid.add(ratingSpinner, 1, 0);
        grid.add(reviewLabel, 0, 1);
        grid.add(reviewField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(ratingSpinner.getValue(), reviewField.getText());
            }
            return null;
        });

        Optional<Pair<Integer, String>> result = dialog.showAndWait();
        result.ifPresent(pair -> {
            int rating = pair.getKey();
            String review = pair.getValue();
            simpanRating(trx.getTransaksiId(), rating, review);
            loadHistori();
        });
    }

    private void simpanRating(int transaksiId, int rating, String review) {
        try (Connection conn = DataSourceManager.getUserConnection()) {
            String insert = "INSERT INTO penilaian(pelanggan_id, transaksi_id, rating, review) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insert);
            ps.setInt(1, HelloApplication.getApplicationInstance().getUserId());
            ps.setInt(2, transaksiId);
            ps.setInt(3, rating);
            ps.setString(4, review);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void buttonBackOrderView(ActionEvent actionEvent) {
        HelloApplication.getApplicationInstance().switchScene("user/order-view.fxml", "Order View");
    }
}
