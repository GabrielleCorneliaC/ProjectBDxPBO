package com.example.bdsqltester;

import com.example.bdsqltester.dtos.OrderItem;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;


public class HelloApplication extends Application {
    private static HelloApplication applicationInstance;
    private int userId;
    private Stage primaryStage;
    public static HelloApplication getApplicationInstance () { return applicationInstance; }
    public Stage getPrimaryStage () { return primaryStage; }


    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return this.userId;
    }

    @Override
    public void start(Stage stage) throws IOException {
        HelloApplication.applicationInstance = this;

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage = stage;

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public void switchScene(String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFileName));
            Scene scene = new Scene(loader.load());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int cabangId;

    public void setCabangId(int id) {
        this.cabangId = id;
    }

    public int getCabangId() {
        return this.cabangId;
    }

    private static ObservableList<OrderItem> currentOrderItems = FXCollections.observableArrayList();

    public static void setCurrentOrderItems(ObservableList<OrderItem> orderItems) {
        currentOrderItems = orderItems;
    }

    public static ObservableList<OrderItem> getCurrentOrderItems() {
        return currentOrderItems;
    }

    public static void main(String[] args) {
        launch();

    }

}