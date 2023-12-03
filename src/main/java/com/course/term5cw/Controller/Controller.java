package com.course.term5cw.Controller;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;


import com.course.term5cw.Common.FileWatcher;
import com.course.term5cw.Model.Adapter;
import com.course.term5cw.Model.Dictionary;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class Controller implements Serializable {

    private HashMap<String, ArrayList<Adapter>> versions = new HashMap<>();

    private Dictionary fabric = new Dictionary();

    private File file = null;

    @FXML
    private transient ChoiceBox<String> choiceVersion;

    @FXML
    private transient TextField versionNameTextField;

    @FXML
    private transient Label fileNameLabel;

    @FXML
    private transient TextArea fileTextArea;

    @FXML
    private transient Button removeVersionButton;

    @FXML
    private ProgressIndicator updateIndicator;

    @FXML
    void initialize() {
        updateIndicator.setOpacity(0.0);
        // Event listener to choice box change
        choiceVersion.setOnAction(event -> {
            String ver = choiceVersion.getValue();
            if (ver != null) {
                try {
                    updateFileWithVersion(ver);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                removeVersionButton.setDisable(true);
            }
        });
    }

    public HashMap<String, ArrayList<Adapter>> getVersions() {
        return versions;
    }

    public void setVersions(HashMap<String, ArrayList<Adapter>> value) {
        versions = value;
    }

    public Dictionary getFabric() {
        return fabric;
    }

    public void setFabric(Dictionary value) {
        fabric = value;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File f) throws IOException {
        file = f; // Set new file
        fileNameLabel.setText(file.getName()); // Change current file text
        startFileChangeThread(f); // Add thread to check file change event

        Scanner in = new Scanner(file, StandardCharsets.UTF_8);
        fileTextArea.clear();
        while (in.hasNextLine()) {
            fileTextArea.appendText(in.nextLine() + '\n');
        }
        in.close();
    }

    private void startFileChangeThread(File f) {
        TimerTask task = (new FileWatcher(f) {
            @Override
            protected void onChange(File file) {
                // Sync current text
                synchronized (fileTextArea) {
                    try {

                        // Set up
                        updateIndicator.setOpacity(1.0F);
                        updateIndicator.setProgress(0.0F); // Set progress to 0%
                        Scanner in = new Scanner(file, StandardCharsets.UTF_8);
                        StringBuffer text = new StringBuffer();
                        fileTextArea.clear();

                        // Read file and fill textArea
                        while (in.hasNextLine()) {
                            text.append(in.nextLine());
                            text.append('\n');
                        }
                        fileTextArea.setText(String.valueOf(text));
                        in.close();

                        // Load indicator animation
                        updateIndicator.setProgress(1.0F);
                        for (double i = 1.0; i > 0; i -= 0.02) {
                            Thread.sleep(10);
                            updateIndicator.setOpacity(i);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        });

        Timer timer = new Timer();
        timer.schedule(task, new Date(), 500);
    }

    private void updateFileWithVersion(String ver) throws IOException {
        if (ver != null) {
            versionNameTextField.setText(ver);
            Writer out = new FileWriter(file, StandardCharsets.UTF_8);
            for (Adapter i : versions.get(ver)) {
                String word = fabric.getWordByAdapter(i);
                if (Pattern.matches("[\\s]", word)) {
                    out.write(fabric.getWordByAdapter(i));
                } else {
                    out.write(fabric.getWordByAdapter(i) + " ");
                }
            }
            out.flush();
            out.close();
        }
    }

    private void savePreviewTextToFile() throws IOException {
        Writer out = new FileWriter(file, StandardCharsets.UTF_8);
        out.write(fileTextArea.getText());
        out.flush();
        out.close();
    }

    @FXML
    void SaveVersionOnClicked(MouseEvent event) throws Exception {
        String version_name = versionNameTextField.getText(); // Get version name
        if (version_name.isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Blank version name").showAndWait();
        } else if (!version_name.equals(choiceVersion.getValue())) {
            // Update dictionary and get array of adapters to save version
            versions.put(version_name, fabric.addVersion(file));
            // Update current choice
            Set<String> keys = versions.keySet(); // Get all map keys = version names set
            choiceVersion.setItems(FXCollections.observableList(keys.stream().toList())); // Replace all choices
            choiceVersion.setValue(version_name); // Select current version
            // Write new version to file
            savePreviewTextToFile();
        } else {
            fabric.removeVersion(versions.get(version_name));
            versions.remove(version_name);
            versions.put(version_name, fabric.addVersion(file));
            savePreviewTextToFile();
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

    public void syncDataWithUI() {
        fileNameLabel.setText(file.getName());
        updateVersionChoiceBox();
    }

    public void updateVersionChoiceBox() {
        choiceVersion.setItems(FXCollections.observableList(versions.keySet().stream().toList()));
        int last_choice_ind = choiceVersion.getItems().size() - 1;
        if (last_choice_ind >= 0) {
            choiceVersion.setValue(choiceVersion.getItems().get(last_choice_ind));
        }
    }
}