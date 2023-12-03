package com.course.term5cw.Controller;

import com.course.term5cw.Main;
import com.course.term5cw.Model.Adapter;
import com.course.term5cw.Model.Dictionary;
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
import java.util.*;

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
            ArrayList<Adapter> tmp = new ArrayList<>();
            HashMap<String, Adapter> dict = controller.getFabric().getDict();
            Set<String> keys = dict.keySet();
            out.println(controller.getFile().getAbsolutePath());
            out.println(dict.size()); // Dictionary size
            if (!dict.isEmpty()) {
                for (String key : keys) {
                    tmp.add(dict.get(key));
                    if (Objects.equals(key, "\n")) {
                        out.println("\\n " + dict.get(key).count); // Word and it's count
                    } else {
                        out.println(key + " " + dict.get(key).count); // Word and it's count
                    }

                }
            }

            HashMap<String, ArrayList<Adapter>> versions = controller.getVersions();
            out.println(controller.getVersions().size()); // Count of file versions
            if (!versions.isEmpty()) {
                for (String version : controller.getVersions().keySet()) {
                    out.println(version); // Version name
                    ArrayList<Adapter> adapters = controller.getVersions().get(version);
                    for (Adapter adapter : adapters) {
                        out.print(tmp.indexOf(adapter) + " "); // Word index in dictionary
                    }
                    out.println(); // Newline for new version
                }
            }
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
            ArrayList<Adapter> tmp = new ArrayList<>();
            HashMap<String, Adapter> dict = controller.getFabric().getDict();
            Set<String> keys = dict.keySet();
            out.write(controller.getFile().getAbsolutePath() + "\n"); // File path
            out.write(dict.size() + "\n"); // Dictionary size
            if (!dict.isEmpty()) {
                for (String key : keys) {
                    tmp.add(dict.get(key));
                    if (Objects.equals(key, "\n")) {
                        out.write(("\\n " + dict.get(key).count) + "\n"); // Word and it's count
                    } else {
                        out.write((key + " " + dict.get(key).count) + "\n"); // Word and it's count
                    }

                }
            }

            HashMap<String, ArrayList<Adapter>> versions = controller.getVersions();
            out.write(controller.getVersions().size() + "\n"); // Count of file versions
            if (!versions.isEmpty()) {
                for (String version : controller.getVersions().keySet()) {
                    out.write(version + "\n"); // Version name
                    ArrayList<Adapter> adapters = controller.getVersions().get(version);
                    for (Adapter adapter : adapters) {
                        out.write((tmp.indexOf(adapter) + " ")); // Word index in dictionary
                    }
                    out.write("\n");
                }
            }
        }

        out.flush();
        out.close();
    }

    @FXML
    void allToSerialized(MouseEvent event) throws IOException {
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
            String path = in.readLine();
            controller.setFile(new File(path));
            controllers.add(controller);
            stage.setOnCloseRequest(e -> controllers.remove(controller));

            int dictionary_size = Integer.parseInt(in.readLine());

            HashMap<String, Adapter> map = new HashMap<>(); // future controller dictionary
            ArrayList<String> words = new ArrayList<>(); // tmp array to translate indexes from file into text

            // Fill dictionary
            for (int j = 0; j < dictionary_size; ++j) {
                String[] line = in.readLine().split(" ");
                if (Objects.equals(line[0], "\\n")) {
                    line[0] = "\n";
                }
                map.put(line[0], new Adapter(Integer.parseInt(line[1])));
                words.add(line[0]);
            }
            controller.setFabric(new Dictionary(map)); // Set new dictionary

            // Fill versions map
            int version_count = Integer.parseInt(in.readLine());
            for (int j = 0; j < version_count; ++j) {
                String version_name = in.readLine();
                String[] indexes = in.readLine().split(" "); // Text as indexes array
                ArrayList<Adapter> version_array = new ArrayList<>(); // Future text as adapter array
                for (String index : indexes) {
                    int num = Integer.parseInt(index);
                    // Find word's adapter by index in tmp array
                    // and add this adapter to text as adapters array
                    version_array.add(map.get(words.get(num)));
                }
                controller.getVersions().put(version_name, version_array); // Add new version
            }

            controller.syncDataWithUI(); // Update UI to display new data
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

            controller.setFabric(tmp_controller.getFabric());
            controller.setVersions(tmp_controller.getVersions());
            controller.setFile(tmp_controller.getFile());

            stage.show();
            controllers.add(controller);
            stage.setOnCloseRequest(e -> controllers.remove(controller));

            controller.syncDataWithUI();
        }
    }
}