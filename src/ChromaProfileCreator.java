/*
 * This file was created by Luke as part of the ChromaCreator project on GitHub located here:
 *   https://github.com/lveilleux/ChromaCreator
 * This project is Open Source to use, & any modifications must make it back to the GitHub repository of this project
 * For questions/comments/problems goto the GitHub page above
 * Enjoy
 */

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Main file for the Chroma Profile Creator application
 * This program takes in an image and converts it into a Chroma profile
 */
public class ChromaProfileCreator {

    private static final String HELP_MESSAGE = "Chroma Profile Creator - Created by LVeilleux\n\n" +
            "For bug fixes and feature requests, checkout this project's GitHub\n" +
            "https://github.com/lveilleux/ChromaCreator\n" +
            "This program allows you to convert any image into a custom RazerChroma static layer, with the\n" +
            "addition of adding an OverLayer on top of the static image.\nCurrently supported OverLayers are:\n" +
            "\t0:None\t1:Reactive Layer\nThe color of this layer depends on the input color name.\n" +
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
                    if (fileName.charAt(0) == '\"' && fileName.charAt(fileName.length() - 1) == '\"')
                        fileName = fileName.substring(1, fileName.length() - 1);
                    //Import the image into program
                    img = importImage(fileName);
                    flag = false;
                } catch (Exception e) {
                    System.out.println("\t\tInvalid File Name.");
                }
            }
        }
        int reactLength = 0;
        if (args.length <= 1) {
            while (layer < 0 || layer > 1) {
                try {
                    System.out.print("Enter a profile over layer (0: None, 1:Reactive): ");
                    layer = in.nextInt();
                } catch (NoSuchElementException ignored) {
                    System.out.println("\tInvalid Input Detected.\tPlease enter valid input [0-1]");
                    in.next();
                }
            }
            try {
                System.out.println("Enter the desired react timing (Short-500, Medium-1500, Long-2000): ");
                reactLength = in.nextInt();
            } catch (NoSuchElementException ignored) {
                System.out.println("Found invalid react time, using default 500.");
                reactLength = 500;
            }
            in.nextLine();
        } else {
            layer = Integer.parseInt(args[1]);
        }
        if (args.length <= 2) {
            if (layer == 1) {
                while (reactColor == null) {
                    try {
                        System.out.print("Enter a color name for the OverLayer (Reactive Layer): ");
                        Field temp = Color.class.getField(in.next().toLowerCase());
                        reactColor = (Color) temp.get(null);
                    } catch (NoSuchFieldException | IllegalAccessException ignored) {
                        System.out.println("\tInvalid Input Detected.\tPlease enter a valid color name.\n" +
                                "\t\t(white, gray, black, red, pink, orange, yellow, green, magenta, cyan, blue)");
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
        //Build & Export Profile
        exportProfile(img, reactColor, layer, reactLength);
        //Save output .razerchroma file to desired location
        String output = "";
        in.nextLine();
        while(output.equals("")) {
            System.out.println("Please enter your output file name (and directory): ");
            output = in.nextLine();
            if (output.charAt(0) == '\"' && output.charAt(output.length() - 1) == '\"')
                output = output.substring(1, output.length() - 1);
        }
        saveFinalOutputFile(new File(output));
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
        img = createResizedCopy(img, XMLCreator.RAZER_KB_WIDTH, XMLCreator.RAZER_KB_HEIGHT, true);
        return img;
    }

    /**
     * Method to Export profile, given the static image and reactive color over the static image.
     * Builds XML files and calls to zip and rename the ending RazerChroma file.
     * layer controls which profile is placed over the static image, (0 - 1) = [None, Reactive]
     * @param img - Image to create the static profile from
     * @param overColor - Color for the Reactive layer
     * @param layer - Which profile to create over the static image. [0=None, 1=Reactive]
     */
    protected static void exportProfile(BufferedImage img, Color overColor, int layer, int reactLength) {
        //Create zipfile for XML to write out to
        try {
            ZipOutput.getReference().createZipFile("Chroma.zip");
            //Build separate XML files
            String staticName = XMLCreator.buildStaticXMLFile(img);
            String reactName = "";
            if (overColor != null) {
                reactName = XMLCreator.buildReactXMLFile(overColor, reactLength);
            }
            //Build Control XML file
            String[] files;
            if (layer != 0)
                files = new String[]{staticName, reactName};
            else
                files = new String[]{staticName};
            XMLCreator.buildControlXMLFile(files);
            ZipOutput.getReference().closeZipFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to call the ZipOutput method that renames the file extension and moves the file to the desired location
     * @param saveFile - Directory and name of where to place the profile.
     */
    protected static void saveFinalOutputFile(File saveFile) {
        //Make sure we are saving this file to a .razerchroma type
        if(!saveFile.toString().toLowerCase().contains(".razerchroma"))
            saveFile = new File(saveFile + ".razerchroma");
        ZipOutput.getReference().moveAndSwapFileExtension(saveFile);
    }

    /**
     * Takes the original given image and resizes it based on input parameters. New resized image is returned.
     * @param originalImage - Original image to resize.
     * @param scaledWidth - New width to make the image.
     * @param scaledHeight - New height to make the image.
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
}
