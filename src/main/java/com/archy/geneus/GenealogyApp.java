package com.archy.geneus;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class GenealogyApp extends javafx.application.Application {

    protected static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void start(Stage primaryStage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/PeopleWindow.fxml"));
        var scene = new Scene(loader.load(), 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Geneus - All People");
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}