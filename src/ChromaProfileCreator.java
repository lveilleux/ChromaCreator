import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    public static void main(String[] args) {
        String fileName = "";
        if (args.length == 0) {
            Scanner in = new Scanner(System.in);
            System.out.print("Please enter your image path and name: ");
            fileName = in.next();
        } else if (args[0].equals("-h")) {
            System.out.println("Help message.");
        } else {
            fileName = args[0];
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileName));
        } catch (IOException e) {
            System.out.println("Please enter a valid image file name.");
            System.exit(32);
        }
        img = createResizedCopy(img, 22, 6, true);
        System.out.println("IMG SIZE: " + img.getHeight() + " x " + img.getWidth());

        buildXMLFiles(img);

        //Run python script to zip files and convert to RazerChroma
        try {
            Process p = Runtime.getRuntime().exec("python src/createRazerChroma.py");
            p.waitFor();
            p.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        //ZipController.zipFiles("output/", "ChromeProfile");
        //File profile = new File("ChromaProfile.zip");
        //boolean success = profile.renameTo( new File("ChromeProfile.razerchroma"));
        //System.out.print(success);
        /*
        //Save the new tiny image out
        fileName = fileName.replace(".jpg", "NEW.jpg");
        try {
            ImageIO.write(img, "JPEG", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }


    private static BufferedImage createResizedCopy(Image originalImage, int scaledWidth, int scaledHeight, boolean preserveAlpha) {
        System.out.println("resizing...");
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

    private static void buildXMLFiles(BufferedImage image) {
        System.out.println("Generated XML files for ChromeProfile");

        //Find all different colors in the image
        final int width = 22, height = 6;
        Color[][] result = new Color[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int RGB = image.getRGB(j, i);
                Color c = new Color(RGB);
                result[i][j] = c;
            }
        }
        //Remove blank keys in the Razer Chroma LEDs
        result = removedUnusedKeys(result);

        boolean done = false;
        int ID = 0;
        try {
            File inputFile = new File("template/static2.xml");
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);
            Element rootElement = document.getRootElement();

            Element keyColorElement = rootElement.getChild("LayerKeys");
            List<Element> list = keyColorElement.getChildren();
            Element colorElement = list.get(0).clone();
            list.clear();
            HashMap<Color, Integer> colorMap = new HashMap<>();
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
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

            // display xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(document, new FileOutputStream("output/2E93D2CA-802D-4B15-8C33-65C1864F5890.xml"));
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void pack(final Path folder, final Path zipFilePath) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFilePath.toFile());
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }

                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    zos.putNextEntry(new ZipEntry(folder.relativize(dir).toString() + "/"));
                    zos.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }


    private static Color[][] removedUnusedKeys(Color[][] keyArray) {
        int[] UNUSED_KEY_X = {0,0, 0, 0, 0, 3, 3, 3, 3, 3,4, 4, 4, 4,5,5,5,5,5, 5, 5, 5};
        int[] UNUSED_KEY_Y = {0,2,18,19,21,13,15,16,17,21,2,13,15,17,4,5,6,8,9,10,18,21};
        for (int i = 0; i < UNUSED_KEY_X.length; i++) {
            keyArray[UNUSED_KEY_X[i]][UNUSED_KEY_Y[i]] = null;
        }
        return keyArray;
    }

}
