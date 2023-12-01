package com.course.term5cw.Controller;

import com.course.term5cw.Main;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    // Controllers array for every new file window
    ArrayList<Controller> controllers = new ArrayList<>();

    @FXML
    void paneOnDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.ANY);
        }
    }

    @FXML
    void paneOnDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        List<File> files = db.getFiles();

        try {
            for (File file : files) {
                newWindow(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        event.setDropCompleted(true);
        event.consume();
    }

    @FXML
    void newFileOnClicked(MouseEvent event) throws IOException {
        // Choose file before show window
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("text file", "*.txt");
        fileChooser.setTitle("Load txt file");
        fileChooser.getExtensionFilters().addAll(extFilter);
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null && file.isFile()) {
            newWindow(file);
        }
    }

    public void newWindow(File file) throws IOException {
        // Open if file exist
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();

        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);
        stage.setTitle("File version control menu");

        Controller c = fxmlLoader.getController(); // Get controller
        c.setFile(file); // Put file into controller
        controllers.add(c); // Add controller to controllers array;

        stage.setOnCloseRequest(e -> {
            controllers.remove(c);
        });
    }

    @FXML
    void allToBinary(MouseEvent event) {

    }

    @FXML
    void allToSerialized(MouseEvent event) {

    }

    @FXML
    void allToTxt(MouseEvent event) {

    }

}