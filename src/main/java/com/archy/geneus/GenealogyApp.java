package com.archy.geneus;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class GenealogyApp extends javafx.application.Application {

    protected static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    protected static FileWriter logWriter;
    protected static File logFile;

    public static void log(String message) {
        var sb = new StringBuilder();
        sb.append("[");
        sb.append(LocalDate.now().toString()).append(" ");
        sb.append(LocalTime.now().toString()).append(" ");
        sb.append("] ");
        sb.append(message);
        System.out.println(sb);
        try {
            logWriter.write(sb.toString());
            logWriter.write("\n");
            logWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        logFile = new File("log.txt");
        if (logFile.exists()) logFile.delete();
        logFile.createNewFile();

        logWriter = new FileWriter(logFile, true);


        var loader = new FXMLLoader(getClass().getResource("/PeopleWindow.fxml"));
        log("Loaded PeopleWindow.fxml");

        var scene = new Scene(loader.load(), 1200, 800);
        log("Loaded scene");

        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Geneus");
        primaryStage.show();
        log("Stage shown");
    }


    public static void main(String[] args) {
        launch(args);
    }
}