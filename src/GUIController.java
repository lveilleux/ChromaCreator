import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controls the GUI and connect to the core code.
 */
public class GUIController implements Initializable {
    @FXML
    private Label inputFileLabel = new Label();
    @FXML
    private Button exportProfileButton = new Button();
    @FXML
    private CheckBox reactiveLayerCheck = new CheckBox();
    @FXML
    private ColorPicker reactiveColor = new ColorPicker();
    private Color reactiveLayerColor;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO: Implement Reactive Layer
        reactiveLayerCheck.setDisable(true);
    }

    @FXML
    protected void handleImageFileIntake(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image for Chroma Profile");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File imageFile = fileChooser.showOpenDialog(stage);
        //Set Label next to button to file path
        if (imageFile != null) {
            inputFileLabel.setText(imageFile.getAbsolutePath());
        }
    }

    @FXML
    protected void handleCreateProfileButton(ActionEvent event) {
        //Run Chroma Creator
        String[] arguments;
        if (reactiveLayerCheck.isSelected()) {
            arguments = new String[]{inputFileLabel.getText(), reactiveColor.getValue().toString()};
        } else {
            arguments = new String[]{inputFileLabel.getText()};
        }
        Runnable runnable = () -> ChromaProfileCreator.main(arguments);
        runnable.run();
        exportProfileButton.setDisable(false);
    }

}
