/*
 * This file was created by Luke as part of the ChromaCreator project on GitHub located here:
 *   https://github.com/lveilleux/ChromaCreator
 * This project is Open Source to use, & any modifications must make it back to the GitHub repository of this project
 * For questions/comments/problems goto the GitHub page above
 * Enjoy
 */

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class created to hold the code to create all the custom XML for the Razer Chroma profiles
 * This includes XML files for Static, & Reactive layers and the Control file.
 */
public class XMLCreator {

    protected static final int RAZER_KB_HEIGHT = 6;
    protected static final int RAZER_KB_WIDTH = 22;

    protected static final String VERSION_NUMBER = "1.49";

    /**
     * Constructor method for the XMLCreator class.
     */
    public XMLCreator() { }

    /**
     * Pulls from the static.xml file template to convert the given image to an xml file that correctly colors all keys
     * that exist in the Razer Master Keyboard layout to the colors given in the image. Returns the name of the
     * file created.
     * @param image - Image used to create static profile
     * @return - Name of the created XML file
     */
    protected String buildStaticXMLFile(BufferedImage image) {
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
            Document document = createLayerXML("STATIC");
            Element rootElement = document.getRootElement();

            Element keyColorElement = rootElement.getChild("LayerKeys");
            java.util.List<Element> list = keyColorElement.getChildren();
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
            java.util.List<Element> groupList = Groups.getChildren();
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
            ZipOutput.getReference().outputToZipFile(document, fileName);
        } catch (IOException e) {
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
    protected static Color[][] removedUnusedKeys(Color[][] keyArray) {
        int[] UNUSED_KEY_X = {0,0, 0, 0, 0, 3, 3, 3, 3, 3,4, 4, 4, 4,5,5,5,5,5, 5, 5, 5};
        int[] UNUSED_KEY_Y = {0,2,18,19,21,13,15,16,17,21,2,13,15,17,4,5,6,8,9,10,18,21};
        for (int i = 0; i < UNUSED_KEY_X.length; i++) {
            keyArray[UNUSED_KEY_X[i]][UNUSED_KEY_Y[i]] = null;
        }
        return keyArray;
    }

    /**
     * Pulls from the reactive.xml file template to create a reactive layer effect over the static image on all
     * keys that exist in the Razer Master Keyboard layout to the colors given in the image. Returns the name of the
     * file created.
     * @param color - Color to show on key press on the keyboard
     * @return - Name of the created XML file
     */
    protected String buildReactXMLFile(Color color, int length) {
        String fileName = "reactive.xml";
        try {
            Document document = createLayerXML("REACTIVE");
            Element rootElement = document.getRootElement();
            Element keyColorElement = rootElement.getChild("LayerKeys");
            java.util.List<Element> list = keyColorElement.getChildren();
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
            java.util.List<Element> groupList = Groups.getChildren();
            Element group = groupList.get(0).clone();
            groupList.clear();
            Element Red = group.getChild("EfxReactive").getChild("Color").getChild("Red");
            Element Green = group.getChild("EfxReactive").getChild("Color").getChild("Green");
            Element Blue = group.getChild("EfxReactive").getChild("Color").getChild("Blue");

            Red.setText(Integer.toString(color.getRed()));
            Green.setText(Integer.toString(color.getGreen()));
            Blue.setText(Integer.toString(color.getBlue()));
            groupList.add(group);
            //Set the duration of the Reactive Layer
            group.getChild("EfxReactive").getChild("Duration").setText(Integer.toString(length));
            ZipOutput.getReference().outputToZipFile(document, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * Pulls from the control.xml template file and creates a control file for the final .razerchroma file that
     * connects and layers all layers created earlier.
     * @param fileNames - Names of all the layer XML files created
     */
    protected void buildControlXMLFile(String[] fileNames) {
        String fileName = "controller.xml";
        try {
            Document document = createControlXML();
            Element rootElement = document.getRootElement();
            java.util.List<Element> tiersList = rootElement.getChildren("Tier");
            Element template = tiersList.get(0).clone();
            tiersList.clear();
            for(int i = 0; i < fileNames.length; i++) {
                fileNames[i] = fileNames[i].replace(".xml", "");
                Element temp = template.clone();
                temp.setAttribute("level", Integer.toString(i));
                temp.getChild("UUID").setText(fileNames[i]);
                tiersList.add(temp);
            }
            ZipOutput.getReference().outputToZipFile(document, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Private function used to create a basic layer XML file used by RazerChroma.
     * Will build basic static and reactive layer files
     * @param effect - Effect type of xml to build ["STATIC", "REACTIVE"]
     * @return - template XML document
     */
    private static Document createLayerXML(String effect){
        Document document = new Document();
        Element root = new Element("RGBLayer");
        root.setAttribute(new Attribute("effect", effect));
        root.setAttribute(new Attribute("enabled", "1"));
        root.addContent(new Element("Version").setText(VERSION_NUMBER));
        //LayerKeys
        Element layerkeys = new Element("LayerKeys");
        Element key = new Element("LayerKey").setAttribute(new Attribute("row", "0"));
        key.setAttribute(new Attribute("column", "7")).setAttribute(new Attribute("id", "0"));
        layerkeys.addContent(key);
        root.addContent(layerkeys);
        // GROUPS
        Element Groups = new Element("Groups");
        Element group = new Element("Group").setAttribute(new Attribute("id", "0"));
        Element efx;
        Element color = new Element("Color").setAttribute(new Attribute("random", "0"));
        if (effect.equals("STATIC")) {
            efx = new Element("EfxStatic");
        } else if (effect.equals("REACTIVE")) {
            efx = new Element("EfxReactive");
            color.setAttribute(new Attribute("id", "1")); //Special Requirement for Reactive Layers
        } else {
            efx = new Element("EFX");
        }
        color.addContent(new Element("Red").setText("0"));
        color.addContent(new Element("Green").setText("255"));
        color.addContent(new Element("Blue").setText("0"));
        efx.addContent(color);
        if(effect.equals("REACTIVE"))
            efx.addContent(new Element("Duration").setText("500"));
        group.addContent(efx);
        Groups.addContent(group);
        root.addContent(Groups);

        document.setContent(root);
        return document;
    }

    /**
     * Private function used to create a basic control XML file used by RazerChroma.
     * Again, currently only supports keyboard profiles.
     * @return - template XML document
     */
    private static Document createControlXML(){
        Document document = new Document();
        Element root = new Element("Tiers");
        root.addContent(new Element("Version").setText(VERSION_NUMBER));
        root.addContent(new Element("DeviceType").setText("KEYBOARD"));
        root.addContent(new Element("PID").setText("544"));
        Element tier = new Element("Tier").setAttribute(new Attribute("level", "0"));
        tier.addContent(new Element("UUID").setText("static"));
        root.addContent(tier);
        document.setContent(root);
        return document;
    }

}
