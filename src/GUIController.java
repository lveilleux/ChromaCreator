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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
    private CheckBox rippleLayerCheck = new CheckBox();
    @FXML
    private ColorPicker reactionColor = new ColorPicker();
    @FXML
    private ImageView imageView = new ImageView();
    @FXML
    private ComboBox<String> reactiveComboBox = new ComboBox<>();
    @FXML
    private Slider rippleLayerSlider = new Slider();

    private BufferedImage keyboardImage;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        keyboardImage = null;
        List<String> list = new ArrayList<String>();
        list.add("Quick");
        list.add("Medium");
        list.add("Long");
        ObservableList obList = FXCollections.observableList(list);
        reactiveComboBox.getItems().clear();
        reactiveComboBox.setItems(obList);
    }

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
        BufferedImage img;
        img = ChromaProfileCreator.importImage(inputFileLabel.getText());
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

    @FXML
    protected void handleReactivePickerChecked(ActionEvent event) {
        //Using Reactive, un-check Ripple
        rippleLayerCheck.setSelected(false);
    }

    @FXML
    protected void handleRipplePickerChecked(ActionEvent event) {
        //Using Reactive, un-check Ripple
        reactiveLayerCheck.setSelected(false);
    }

    @FXML
    protected void handleExportProfileButton(ActionEvent event) throws IOException {
        //Export Profile
        Color layerColor;
        java.awt.Color color = null;
        int layer = 0;
        if(reactiveLayerCheck.isSelected() || rippleLayerCheck.isSelected()) {
            layerColor = reactionColor.getValue();
            color = new java.awt.Color((float) layerColor.getRed(),
                    (float) layerColor.getGreen(),
                    (float) layerColor.getBlue(),
                    (float) layerColor.getOpacity());
        }
        if(reactiveLayerCheck.isSelected()) {
            layer = 1;
        } else if (rippleLayerCheck.isSelected()) {
            layer = 2;
        }
        //TODO: Support Ripple Speed and Reactive Length
        File temp = new File("output");
        if(temp.mkdir() || temp.exists()) {
            ChromaProfileCreator.exportProfile(keyboardImage, color, layer);
            for (File file : temp.listFiles()) {
                Files.delete(file.toPath());
            }
            temp.delete();
        } else {
            throw new IOException("Failed to create temporary directory, please try again");
        }
        System.out.println("Profile Created");
    }
}
