package com.course.term5cw.Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import com.course.term5cw.Model.AdapterFlyweight;
import com.course.term5cw.Model.DictionaryFabric;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Controller {

    HashMap<String, ArrayList<AdapterFlyweight>> versions = new HashMap<>();

    File file = null;

    @FXML
    private ChoiceBox<String> choiceVersion;

    @FXML
    private TextField versionNameTextField;

    @FXML
    private Label fileNameLabel;

    @FXML
    private TextArea fileTextArea;

    @FXML
    void initialize() {
        choiceVersion.setOnAction(event -> {
            // Преобразовать массив в текст
            // Записать текст в TextArea
            // Перезаписать файл типо версия сменилась POG
        });
    }

    @FXML
    void SaveVersionOnClicked(MouseEvent event) throws Exception {
        String version_name = versionNameTextField.getText(); // Get version name
        if (!version_name.isBlank()) {
            DictionaryFabric fabric = new DictionaryFabric();
            versions.put(version_name, fabric.processFile(file)); // Add version name to versions
            Set<String> keys = versions.keySet(); // Get all map keys = version names set
            choiceVersion.setItems(FXCollections.observableList(keys.stream().toList())); // Replace all choices
            choiceVersion.setValue(version_name); // Select current version
        } else {
            new Alert(Alert.AlertType.WARNING, "Blank version name").showAndWait();
        }
    }

    @FXML
    void saveTextOnClicked(MouseEvent event) throws IOException {
        Writer out = new FileWriter(file);
        out.write(fileTextArea.getText());
        out.flush();
        out.close();
        new Alert(Alert.AlertType.INFORMATION, "File successfully saved").showAndWait();
    }

    public void setFile(File f) throws IOException {
        file = f;
        fileNameLabel.setText(file.getName());

        Scanner in = new Scanner(file, StandardCharsets.UTF_8);
        while (in.hasNextLine()) {
            fileTextArea.appendText(in.nextLine() + '\n');
        }
        in.close();
    }
}