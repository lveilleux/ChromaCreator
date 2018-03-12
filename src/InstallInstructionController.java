/*
 * This file was created by Luke as part of the ChromaCreator project on GitHub located here:
 *   https://github.com/lveilleux/ChromaCreator
 * This project is Open Source to use, & any modifications must make it back to the GitHub repository of this project
 * For questions/comments/problems goto the GitHub page above
 * Enjoy
 */

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the GUI and connects to the main implementation code.
 */
public class InstallInstructionController implements Initializable {
    //App ID number for what instruction set to load.
    public enum AppId { RAZER2, RAZER3, CUE}

    //References to different GUI components
    @FXML
    private ImageView instructionImage = new ImageView();

    private AppId instructionSet = AppId.RAZER2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        instructionImage.setImage();
    }

    public void setInstructionSet(AppId app) {
        instructionSet = app;
    }

    @FXML
    protected void handleButtonLeft(ActionEvent event) {
        //TODO: Implement
    }

    @FXML
    protected void handleButtonRight(ActionEvent event) {
        //TODO: Implement
    }
}

