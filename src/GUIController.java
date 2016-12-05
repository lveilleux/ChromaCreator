import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
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
    @FXML
    private ImageView imageView = new ImageView();

    private Color reactiveLayerColor;
    private BufferedImage keyboardImage;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //TODO: Implement Reactive Layer
        reactiveLayerCheck.setDisable(true);
        keyboardImage = null;
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
        BufferedImage img;
        img = ChromaProfileCreator.importImage(inputFileLabel.getText());
        exportProfileButton.setDisable(false);
        keyboardImage = img;

        //Convert from BufferedImage to JavaFX Image
        WritableImage image = null;
        image = new WritableImage(img.getWidth(), img.getHeight());
        PixelWriter pw = image.getPixelWriter();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                pw.setArgb(x, y, img.getRGB(x, y));
            }
        }
        imageView.setImage(image);
    }

    @FXML
    protected void handleExportProfileButton(ActionEvent event) {
        //Export Profile
        ChromaProfileCreator.exportProfile(keyboardImage, null);
    }

}