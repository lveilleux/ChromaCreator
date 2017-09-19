/*
 * This file was created by Luke as part of the ChromaCreator project on GitHub located here:
 *   https://github.com/lveilleux/ChromaCreator
 * This project is Open Source to use, & any modifications must make it back to the GitHub repository of this project
 * For questions/comments/problems goto the GitHub page above
 * Enjoy
 */

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controls the GUI and connects to the main implementation code.
 */
public class GUIController implements Initializable {
    //References to different GUI components
    @FXML
    private Label inputFileLabel = new Label();
    @FXML
    private Button exportProfileButton = new Button();
    @FXML
    private CheckBox reactiveLayerCheck = new CheckBox();
    @FXML
    private ColorPicker reactionColor = new ColorPicker();
    @FXML
    private ImageView imageView = new ImageView();
    @FXML
    private ComboBox<String> reactiveComboBox = new ComboBox<>();
    private BufferedImage keyboardImage;


    /**
     * Initializes the the keyboard image reference stored within the GUI class, for the image view, and the combo
     * box on the page that controls the reactive layer effect length.
     * @param location - Unused
     * @param resources - Unused
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        keyboardImage = null;
        List<String> list = new ArrayList<String>();
        list.add("Short");
        list.add("Medium");
        list.add("Long");
        ObservableList obList = FXCollections.observableList(list);
        reactiveComboBox.getItems().clear();
        reactiveComboBox.setItems(obList);
        reactiveComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Handles the input of files by allowing the user to use the file picker in the OS to select an image file for
     * mapping to the keyboard.
     * @param event - ActionEvent that allows access to the GUI elements
     */
    @FXML
    protected void handleImageFileIntake(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image for Chroma Profile");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"),
                new FileChooser.ExtensionFilter("BMP", "*.bmp")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File imageFile = fileChooser.showOpenDialog(stage);
        //Set Label next to button to file path
        if (imageFile != null) {
            inputFileLabel.setText(imageFile.getAbsolutePath());
        }
        //Run Chroma Creator
        BufferedImage img = null;
        try {
            img = ChromaProfileCreator.importImage(inputFileLabel.getText());
        } catch (FileNotFoundException f) {
            f.printStackTrace();
            System.exit(45);
        }
        exportProfileButton.setDisable(false);
        keyboardImage = img;

        //Convert from BufferedImage to JavaFX Image
        WritableImage image;
        image = new WritableImage(img.getWidth(), img.getHeight());
        PixelWriter pw = image.getPixelWriter();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pw.setArgb(x, y, img.getRGB(x, y));
            }
        }
        imageView.setImage(image);
    }

    /**
     * Handles the actions that take place behind the scenes to build and export a profile into the .razerchroma
     * file format. Allows the user to select a save location for the file using the OS file picker.
     * @param event - ActionEvent that allows access to the GUI elements
     */
    @FXML
    protected void handleExportProfileButton(ActionEvent event) {
        //Export Profile
        Color layerColor;
        java.awt.Color color = null;
        int layer = 0;
        if(reactiveLayerCheck.isSelected()) {
            layerColor = reactionColor.getValue();
            color = new java.awt.Color((float) layerColor.getRed(),
                    (float) layerColor.getGreen(),
                    (float) layerColor.getBlue(),
                    (float) layerColor.getOpacity());
        }
        if(reactiveLayerCheck.isSelected()) {
            layer = 1;
        }
        int reactLength = 0;
        String react = reactiveComboBox.getSelectionModel().getSelectedItem();
        if (react == null)
            reactLength = 500;
        else if (react.equals("Short"))
            reactLength = 500;
        else if (react.equals("Medium"))
            reactLength = 1500;
        else if (react.equals("Long"))
            reactLength = 2000;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save Location");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("RazerChroma", "*.razerchroma")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File saveFile = fileChooser.showSaveDialog(stage);
        if (saveFile == null){
            return;
        }
        ChromaProfileCreator.exportProfile(keyboardImage, color, layer, reactLength);
        ChromaProfileCreator.saveFinalOutputFile(saveFile);
        //Alert the user the profile was created
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Your RazerChroma profile was successfully created");
        alert.showAndWait();
    }
}
