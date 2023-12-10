package com.course.term5cw.Controller;

import com.course.term5cw.Common.FileWatcher;
import com.course.term5cw.Model.Dictionary;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements Serializable {

    // Working file
    private File file = null;

    // Model and fabric to work with versions
    private Dictionary fabric = new Dictionary();

    @FXML
    private transient Label fileNameLabel;

    @FXML
    private transient Label versionLabel;

    @FXML
    private transient TextArea fileTextArea;

    @FXML
    private transient Button removeVersionButton;

    @FXML
    private transient Button prevVersionButton;

    @FXML
    private transient Button nextVersionButton;

    @FXML
    private transient ProgressIndicator updateIndicator;

    @FXML
    void initialize() {
        versionLabel.setText("None");
        prevVersionButton.setDisable(true);
        nextVersionButton.setDisable(true);
        removeVersionButton.setDisable(true);
        updateIndicator.setOpacity(0.0);
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
        // Set new file
        file = f;
        fileNameLabel.setText(file.getName());

        // Update TextArea preview
        Scanner in = new Scanner(file, StandardCharsets.UTF_8);
        fileTextArea.clear();
        while (in.hasNextLine()) {
            fileTextArea.appendText(in.nextLine() + '\n');
        }
        in.close();

        // Add thread to check OnFileChange event
        startFileChangeThread(f);
    }

    private void startFileChangeThread(File f) {
        TimerTask task = (new FileWatcher(f) {
            @Override
            protected synchronized void onChange(File f) {
                try {
                    // Set up
                    updateIndicator.setOpacity(1.0F);
                    updateIndicator.setProgress(0.0F); // Set progress to 0%
                    Scanner in = new Scanner(f, StandardCharsets.UTF_8);
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
        });

        Timer timer = new Timer();
        timer.schedule(task, new Date(), 250);
    }

    @FXML
    void SaveVersionOnClicked(MouseEvent event) throws Exception {
        fabric.addVersion(file);
        manageUI();
        saveJSON();
    }

    @FXML
    void removeVersionOnClicked(MouseEvent event) throws IOException {
        fabric.removeVersion();
        manageUI();
        saveJSON();
    }

    @FXML
    void prevButtonOnClicked(MouseEvent event) throws IOException {
        fabric.prevVersion();
        manageUI();
    }

    @FXML
    void nextButtonOnClicked(MouseEvent event) throws IOException {
        fabric.nextVersion();
        manageUI();
    }

    public synchronized void manageUI() throws IOException {
        fileTextArea.clear();
        fileTextArea.setText(fabric.getText());
        FileWriter out = new FileWriter(file);
        out.write(fabric.getText());
        out.close();
        manageVersionNumber();
        manageButtons();
    }

    private void manageVersionNumber() {
        if (fabric.getCurrentVersionIndex() < 0) {
            versionLabel.setText("None");
        } else {
            versionLabel.setText(fabric.getCurrentVersionIndex().toString());
        }
    }

    private void manageButtons() {
        prevVersionButton.setDisable(fabric.getCurrentVersionIndex() <= 0);
        nextVersionButton.setDisable(fabric.getCurrentVersionIndex() >= fabric.getVersionsCount() - 1);
        removeVersionButton.setDisable(fabric.getVersionsCount() == 0);
    }

    private void saveJSON() throws IOException {
        String json = fabric.generateJSON();
        if (!json.isEmpty()) {
            File f = new File(file.getName() + ".json");
            FileWriter out = new FileWriter(f);
            out.write(json);
            out.close();
        }
    }

    @FXML
    void loadJsonOnClicked(MouseEvent event) throws IOException {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("JSON file", "*.json");
        fileChooser.setTitle("Load json file");
        fileChooser.getExtensionFilters().addAll(extFilter);
        fileChooser.setInitialDirectory(new File("."));
        File f = fileChooser.showOpenDialog(new Stage());

        if (f != null && f.isFile()) {
            fabric.loadJSON(f);
            manageUI();
        }
    }
}