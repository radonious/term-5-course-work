package com.course.term5cw.View;

import com.course.term5cw.Controller.MainController;
import com.course.term5cw.Main;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;

import java.io.IOException;

public class MainView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        MainController controller = fxmlLoader.getController();
        stage.setTitle("Version control HUB");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}