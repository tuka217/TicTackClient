package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by ania on 21.05.17.
 */
public class Client extends Application
{

    private Scene scene;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("window.fxml"));

        if (scene == null)
           scene = new Scene(root,920, 330);

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
    }
}
