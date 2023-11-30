package com.course.term5cw.Controller;

import com.course.term5cw.Main;
import javafx.fxml.FXML;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainController {

    // Controllers array for every new file window
    ArrayList<Controller> controllers = new ArrayList<>();

    @FXML
    void createWindow(MouseEvent event) throws IOException {
        // Choose file before show window
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("text file", "*.txt");
        fileChooser.setTitle("Load txt file");
        fileChooser.getExtensionFilters().addAll(extFilter);
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null && file.isFile()) {
            // Open if file exist
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
            stage.setResizable(false);
            stage.setTitle("File version control - using flyweight pattern");

            Controller c = fxmlLoader.getController(); // Get controller
            c.setFile(file); // Put file into controller
            controllers.add(c); // Add controller to controllers array;
        } else {
            new Alert(Alert.AlertType.WARNING, "File was not chosen").showAndWait();
        }
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