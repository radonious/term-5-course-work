package com.course.term5cw.Controller;

import com.course.term5cw.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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
        fileChooser.setInitialDirectory(new File("."));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (!files.isEmpty()) {
            for (File file : files) {
                newWindow(file);
            }
        }
    }

    private void newWindow(File file) throws IOException {
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

        stage.setOnCloseRequest(e -> controllers.remove(c));
    }

    @FXML
    void allToTxt(MouseEvent event) throws FileNotFoundException {
        PrintWriter out = new PrintWriter("state_save.txt");

        out.println(controllers.size()); // Count of controllers/files
        for (Controller controller : controllers) {
            out.println(controller.getFile().getAbsolutePath()); // Working file
            out.println(controller.getFile().getName() + ".json"); // Versions JSON
        }

        out.flush();
        out.close();
    }


    @FXML
    void allToBinary(MouseEvent event) throws IOException {
        // Create bin file writer
        OutputStream os = Files.newOutputStream(Path.of("state_save.bin"));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

        out.write(controllers.size() + "\n"); // Count of controllers/files
        for (Controller controller : controllers) {
            out.write(controller.getFile().getAbsolutePath() + '\n'); // Working file
            out.write(controller.getFile().getName() + ".json" + '\n'); // Versions JSON
        }

        out.flush();
        out.close();
    }

    @FXML
    void allToSerialized(MouseEvent event) throws IOException {
        controllers.forEach(controller -> controller.getFabric().syncAdaptersBeforeSerialization());
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("state_save.ser"));
        oos.writeObject(controllers);
        oos.close();
    }

    private void loadFrom(String source) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(source)); // Can read both .txt and .bin files
        int controllers_count = Integer.parseInt(in.readLine());
        for (int i = 0; i < controllers_count; ++i) {
            // Create new frame for every controller
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("File version control menu");
            stage.show();

            // Get controller to modify
            Controller controller = fxmlLoader.getController();

            // Set working file
            controller.setFile(new File(in.readLine()));
            controller.getFabric().loadJSON(new File(in.readLine()));
            controllers.add(controller);
            stage.setOnCloseRequest(e -> controllers.remove(controller));

            controller.manageUI();
        }
    }

    @FXML
    void allFromTxt(MouseEvent event) throws IOException {
        loadFrom("state_save.txt");
    }

    @FXML
    void allFromBinary(MouseEvent event) throws IOException {
        loadFrom("state_save.bin");
    }

    @FXML
    void allFromSerialized(MouseEvent event) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("state_save.ser"));
        ArrayList<Controller> tmp_controllers = (ArrayList<Controller>) ois.readObject();
        for (Controller tmp_controller : tmp_controllers) {
            // Create new frame for every controller
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.setTitle("File version control menu");

            // Get controller to modify
            Controller controller = fxmlLoader.getController();

            controller.setFile(tmp_controller.getFile());
            controller.setFabric(tmp_controller.getFabric());

            stage.show();
            controllers.add(controller);
            stage.setOnCloseRequest(e -> controllers.remove(controller));

            controller.getFabric().syncAdaptersAfterSerialization();
            controller.manageUI();
        }
    }
}