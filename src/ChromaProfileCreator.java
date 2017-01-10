import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
/**
 * Created by Luke Veilleux
 * Main file for the Chroma Profile Creator application
 * This program takes in an image and converts it into a Chroma profile
 */
public class ChromaProfileCreator {

    private static final int RAZER_KB_HEIGHT = 6;
    private static final int RAZER_KB_WIDTH = 22;

    private static final String HELP_MESSAGE = "Chroma Profile Creator - Created by LVeilleux\n\n" +
            "For bug fixes and feature requests, checkout this project's GitHub\n" +
            "https://github.com/lveilleux/ChromaCreator\n" +
            "This program allows you to convert any image into a custom RazerChroma static layer, with the\n" +
            "addition of adding an OverLayer on top of the static image.\nCurrently supported OverLayers are:\n" +
            "\t0:None\t1:Reactive Layer\t2:Ripple Layer\nThe color of this layer depends on the input color name.\n" +
            "\nCommand Line Arguments:\n" +
            "\t-h | -help: Display this message and exit.\n" +
            "\tChromaProfileCreator [Path to Image] [Type of OverLayer] [String color name for OverLayer]";

    /**
     * Main function used when running the program without the GUI interface, calls the same methods, but requires
     * options be set as arguments at runtime.
     * @param args - System/profile configuration. [Image Path, React Color]
     */
    public static void main(String[] args) {
        String fileName;
        Color reactColor = null;
        int layer = -1;
        Scanner in = new Scanner(System.in);
        if (args.length >= 1 &&  (args[0].equalsIgnoreCase("-h") || args[0].equalsIgnoreCase("-help"))) {
            System.out.println(HELP_MESSAGE);
            System.exit(0);
        }
        BufferedImage img = null;
        boolean flag = false;
        if (args.length >= 1) {
            fileName = args[0];
            try {
                img = importImage(fileName);
            } catch (Exception ignored) {
                System.out.println("Invalid FileName given as argument.");
                flag = true;
            }
        }
        if (flag || args.length == 0) {
            flag = true;
            while (flag) {
                try {
                    System.out.print("Please enter your image path and name: ");
                    fileName = in.nextLine();
                    //Import the image into program
                    img = importImage(fileName);
                    flag = false;
                } catch (Exception e) {
                    System.out.println("\t\tInvalid File Name.");
                }
            }
        }
        if (args.length <= 1) {
            while (layer < 0 || layer > 2) {
                try {
                    System.out.print("Enter a profile over layer (0: None, 1:Reactive, 2:Ripple): ");
                    layer = in.nextInt();
                } catch (NoSuchElementException ignored) {
                    System.out.println("\tInvalid Input Detected.\tPlease enter valid input [0-2]");
                    in.next();
                }
            }
        } else {
            layer = Integer.parseInt(args[1]);
        }
        if (args.length <= 2) {
            if (layer == 1 || layer == 2) {
                while (reactColor == null) {
                    try {
                        System.out.print("Enter a color name for the OverLayer (Ripple/Reactive Layer): ");
                        Field temp = Color.class.getField(in.next().toLowerCase());
                        reactColor = (Color) temp.get(null);
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {
                        System.out.println("\tInvalid Input Detected.\tPlease enter a valid color name.");
                    }
                }
            }
        } else {
            try {
                Field tmp = Color.class.getField(args[2]);
                reactColor = (Color) tmp.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                System.exit(34);
            }
        }
        try {
            File temp = new File("output");
            if (temp.mkdir() || temp.exists()) {
                //Build & Export Profile
                exportProfile(img, reactColor, layer);
                for (File file : temp.listFiles()) {
                    Files.delete(file.toPath());
                }
                temp.delete();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            System.exit(99);
        }
        System.out.println("Profile Created");
    }

    /**
     * Method to import an image into ChromaCreator. This is done by taking in the image and converting it down
     * to the size of the keyboard, in this case the final image is coded to be 22 x 6.
     * @param fileName - Name of the image file to import.
     * @return - A copy of the image in the reduced size, to be shown on the GUI.
     * @throws FileNotFoundException - Image file not found/Invalid
     */
    protected static BufferedImage importImage(String fileName) throws FileNotFoundException {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            throw new FileNotFoundException(fileName);
        }
        String name = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
        img = createResizedCopy(img, RAZER_KB_WIDTH, RAZER_KB_HEIGHT, true);
        return img;
    }

    /**
     * Method to Export profile, given the static image and reactive/ripple color over the static image.
     * Builds XML files and calls to zip and rename the ending RazerChroma file.
     * layer controls which profile is placed over the static image, (0 - 2) = [None, Reactive, Ripple]
     * @param img - Image to create the static profile from
     * @param overColor - Color for the Reactive/Ripple layer
     * @param layer - Which profile to create over the static image. [0=None, 1=Reactive, 2=Ripple]
     */
    protected static void exportProfile(BufferedImage img, Color overColor, int layer) {
        //TODO: Implement Ripple Effect Creation.
        //Build separate XML files
        String staticName = buildStaticXMLFile(img);
        String reactName = "";
        if (overColor != null) {
            reactName = buildReactXMLFile(overColor);
        }
        //Build Control XML file
        String[] files;
        if (layer == 1)
            files = new String[]{staticName, reactName};
        else
            files = new String[]{staticName};
        buildControlXMLFile(files);

        //Run python script to zip files and convert to RazerChroma
        //TODO: Convert back into Java, to remove Python dependency.
        try {
            Process p = Runtime.getRuntime().exec("python src/createRazerChroma.py --name=chromaFile");
            p.waitFor();
            p.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes the original given image and resizes it based on input parameters. New resized image is returned.
     * @param originalImage - Original image to resize.
     * @param scaledWidth - New width to make the image.
     * @param scaledHeight - New height to make the iamge.
     * @param preserveAlpha - Preserve the alpha factor of the image, or not.
     * @return - Resized image based on originalImage with the size of scaledWidth x scaledHeight.
     */
    private static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
        Graphics2D g = scaledBI.createGraphics();
        if (preserveAlpha) {
            g.setComposite(AlphaComposite.Src);
        }
        g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null);
        g.dispose();
        return scaledBI;
    }

    /**
     * Pulls from the static.xml file template to convert the given image to an xml file that correctly colors all keys
     * that exist in the Razer Master Keyboard layout to the colors given in the image. Returns the name of the
     * file created.
     * @param image - Image used to create static profile
     * @return - Name of the created XML file
     */
    private static String buildStaticXMLFile(BufferedImage image) {
        //Find all different colors in the image
        Color[][] result = new Color[RAZER_KB_HEIGHT][RAZER_KB_WIDTH];

        for (int i = 0; i < RAZER_KB_HEIGHT; i++) {
            for (int j = 0; j < RAZER_KB_WIDTH; j++) {
                int RGB = image.getRGB(j, i);
                Color c = new Color(RGB);
                result[i][j] = c;
            }
        }
        //Remove blank keys in the Razer Chroma LEDs
        result = removedUnusedKeys(result);

        int ID = 0;
        String fileName = "static.xml";
        try {
            File inputFile = new File("template/static.xml");
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);
            Element rootElement = document.getRootElement();

            Element keyColorElement = rootElement.getChild("LayerKeys");
            List<Element> list = keyColorElement.getChildren();
            Element colorElement = list.get(0).clone();
            list.clear();
            HashMap<Color, Integer> colorMap = new HashMap<>();
            for (int row = 0; row < RAZER_KB_HEIGHT; row++) {
                for (int col = 0; col < RAZER_KB_WIDTH; col++) {
                    if (result[row][col] == null)
                        continue;
                    Color c = result[row][col];
                    if (!colorMap.containsKey(c))
                        colorMap.put(c, ID++);
                    Element copy = colorElement.clone();
                    copy.setAttribute("row", Integer.toString(row));
                    copy.setAttribute("column", Integer.toString(col));
                    copy.setAttribute("id", Integer.toString(colorMap.get(c)));
                    list.add(copy);
                }
            }
            //Set the Groups based on the IDs in the colorMap
            Element Groups = rootElement.getChild("Groups");
            List<Element> groupList = Groups.getChildren();
            Element groupTemplate = groupList.get(0).clone();
            groupList.clear();

            for( Color color : colorMap.keySet()) {
                Element group = groupTemplate.clone();
                group.setAttribute("id", Integer.toString(colorMap.get(color)));
                Element Red = group.getChild("EfxStatic").getChild("Color").getChild("Red");
                Element Green = group.getChild("EfxStatic").getChild("Color").getChild("Green");
                Element Blue = group.getChild("EfxStatic").getChild("Color").getChild("Blue");

                Red.setText(Integer.toString(color.getRed()));
                Green.setText(Integer.toString(color.getGreen()));
                Blue.setText(Integer.toString(color.getBlue()));
                groupList.add(group);
            }
            XMLOutputter xmlOutput = new XMLOutputter();
            // output xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream("output/" + fileName);
            xmlOutput.output(document, output);
            output.close();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Function used to remove keys that don't exist in the Razer Master Keyboard from the image. The X and Y
     * of each key space not in use is stored in the arrays, and set to null in the key array given.
     * Leaving these keys in causes Synapse/Chroma Configurator to not read the .razerchroma file
     * @param keyArray - 2D Color Array of colors for the image, after downsizing
     * @return - 2D Color array with nonexistent keys(Not in Razer MasterKeyboard) set to null.
     */
    private static Color[][] removedUnusedKeys(Color[][] keyArray) {
        int[] UNUSED_KEY_X = {0,0, 0, 0, 0, 3, 3, 3, 3, 3,4, 4, 4, 4,5,5,5,5,5, 5, 5, 5};
        int[] UNUSED_KEY_Y = {0,2,18,19,21,13,15,16,17,21,2,13,15,17,4,5,6,8,9,10,18,21};
        for (int i = 0; i < UNUSED_KEY_X.length; i++) {
            keyArray[UNUSED_KEY_X[i]][UNUSED_KEY_Y[i]] = null;
        }
        return keyArray;
    }

    /**
     * Pulls from the reactive.xml file template to convert the given image to an xml file that correctly colors all
     * keys that exist in the Razer Master Keyboard layout to the colors given in the image. Returns the name of the
     * file created.
     * @param color - Color to show on key press on the keyboard
     * @return - Name of the created XML file
     */
    private static String buildReactXMLFile(Color color) {
        String fileName = "reactive.xml";
        try {
            File inputFile = new File("template/reactive.xml");
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);
            Element rootElement = document.getRootElement();
            Element keyColorElement = rootElement.getChild("LayerKeys");
            List<Element> list = keyColorElement.getChildren();
            Element colorElement = list.get(0).clone();
            list.clear();
            HashMap<Color, Integer> colorMap = new HashMap<>();
            for (int row = 0; row < RAZER_KB_HEIGHT; row++) {
                for (int col = 0; col < RAZER_KB_WIDTH; col++) {
                    Element copy = colorElement.clone();
                    copy.setAttribute("row", Integer.toString(row));
                    copy.setAttribute("column", Integer.toString(col));
                    copy.setAttribute("id", Integer.toString(0));
                    list.add(copy);
                }
            }

            //Set the Groups based on the IDs in the colorMap
            Element Groups = rootElement.getChild("Groups");
            List<Element> groupList = Groups.getChildren();
            Element group = groupList.get(0).clone();
            groupList.clear();

            //Leaving the group ID, not supporting different Reactive colors
            Element Red = group.getChild("EfxReactive").getChild("Color").getChild("Red");
            Element Green = group.getChild("EfxReactive").getChild("Color").getChild("Green");
            Element Blue = group.getChild("EfxReactive").getChild("Color").getChild("Blue");

            Red.setText(Integer.toString(color.getRed()));
            Green.setText(Integer.toString(color.getGreen()));
            Blue.setText(Integer.toString(color.getBlue()));
            groupList.add(group);

            XMLOutputter xmlOutput = new XMLOutputter();
            // output xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream("output/" + fileName);
            xmlOutput.output(document, output);
            output.close();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Pulls from the control.xml template file and creates a control file for the final .razerchroma file that
     * connects and layers all layers created earlier.
     * @param fileNames - Names of all the layer XML files created
     */
    private static void buildControlXMLFile(String[] fileNames) {
        String fileName = "controller.xml";
        try {
            File inputFile = new File("template/control.xml");
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);
            Element rootElement = document.getRootElement();
            List<Element> tiersList = rootElement.getChildren("Tier");
            Element template = tiersList.get(0).clone();
            tiersList.clear();
            for(int i = 0; i < fileNames.length; i++) {
                fileNames[i] = fileNames[i].replace(".xml", "");
                Element temp = template.clone();
                temp.setAttribute("level", Integer.toString(i));
                temp.getChild("UUID").setText(fileNames[i]);
                tiersList.add(temp);
            }

            XMLOutputter xmlOutput = new XMLOutputter();
            // output xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream("output/" + fileName);
            xmlOutput.output(document, output);
            output.close();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }
}
