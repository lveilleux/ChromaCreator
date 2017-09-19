/*
 * This file was created by Luke as part of the ChromaCreator project on GitHub located here:
 *   https://github.com/lveilleux/ChromaCreator
 * This project is Open Source to use, & any modifications must make it back to the GitHub repository of this project
 * For questions/comments/problems goto the GitHub page above
 * Enjoy
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChromaCreatorGUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(this.getClass().getResource("main.fxml"));
            Scene home = new Scene(root, 800.0D, 600.0D);
            primaryStage.setScene(home);
            primaryStage.setTitle("Chroma Profile Creator");
            primaryStage.show();
        } catch (IOException e) {
            System.out.println("IO Error occurred loading the GUI, please try again.\nIf the problem" +
                    " persists, contact the developers at the GitHub repository.");
            e.printStackTrace();
        }
    }
}
