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
            Scene home = new Scene(root, 600.0D, 400.0D);
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
