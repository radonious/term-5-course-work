package com.course.term5cw.Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import com.course.term5cw.Model.Adapter;
import com.course.term5cw.Model.Dictionary;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class Controller {

    HashMap<String, ArrayList<Adapter>> versions = new HashMap<>();

    Dictionary fabric = new Dictionary();

    File file = null;

    @FXML private ChoiceBox<String> choiceVersion;

    @FXML private TextField versionNameTextField;

    @FXML private Label fileNameLabel;

    @FXML private TextArea fileTextArea;

    @FXML private Button removeVersionButton;

    public void setFile(File f) throws IOException {
        file = f;
        fileNameLabel.setText(file.getName());

        Scanner in = new Scanner(file, StandardCharsets.UTF_8);
        while (in.hasNextLine()) {
            fileTextArea.appendText(in.nextLine() + '\n');
        }
        in.close();
    }

    @FXML
    void initialize() {
        // Event listener to choice box change
        choiceVersion.setOnAction(event -> {
            String ver = choiceVersion.getValue();
            if (ver != null) {
                versionNameTextField.setText(ver);
                removeVersionButton.setDisable(false);
                fileTextArea.clear();
                for (Adapter i : versions.get(ver)) {
                    String word = fabric.getWordByAdapter(i);
                    if (Pattern.matches("[\\s]", word)) {
                        // No space after whitespace chars
                        fileTextArea.appendText(fabric.getWordByAdapter(i));
                    } else {
                        fileTextArea.appendText(fabric.getWordByAdapter(i) + " ");
                    }
                }
                try {
                    saveTextAreaToFile();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                removeVersionButton.setDisable(true);
            }
        });
    }

    void saveTextAreaToFile() throws IOException {
        Writer out = new FileWriter(file, StandardCharsets.UTF_8);
        out.write(fileTextArea.getText());
        out.flush();
        out.close();
    }

    @FXML
    void SaveVersionOnClicked(MouseEvent event) throws Exception {
        String version_name = versionNameTextField.getText(); // Get version name
        System.out.println(version_name + " : " + choiceVersion.getValue());
        if (version_name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Blank version name").showAndWait();
        } else if (!version_name.equals(choiceVersion.getValue())) {
            // Write new version to file
            saveTextAreaToFile();
            // Update dictionary and get array of adapters to save version
            versions.put(version_name, fabric.addVersion(file));
            // Update current choice
            Set<String> keys = versions.keySet(); // Get all map keys = version names set
            choiceVersion.setItems(FXCollections.observableList(keys.stream().toList())); // Replace all choices
            choiceVersion.setValue(version_name); // Select current version
        } else {
            System.out.println("UPDATE");
            fabric.removeVersion(versions.get(version_name));
            saveTextAreaToFile();
            versions.remove(version_name);
            versions.put(version_name, fabric.addVersion(file));
        }
    }

    @FXML
    void removeVersionOnClicked(MouseEvent event) {
        String choice_version = choiceVersion.getValue();
        fabric.removeVersion(versions.get(choice_version));
        versions.remove(choice_version);

        Set<String> keys = versions.keySet(); // Get all map keys = version names set
        choiceVersion.setItems(FXCollections.observableList(keys.stream().toList())); // Replace all choices
        int last_choice_ind = choiceVersion.getItems().size() - 1;
        if (last_choice_ind >= 0) {
            choiceVersion.setValue(choiceVersion.getItems().get(last_choice_ind));
        }
    }
}