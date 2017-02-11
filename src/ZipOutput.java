/*
 * This file was created by Luke as part of the ChromaCreator project on GitHub located here:
 *   https://github.com/lveilleux/ChromaCreator
 * This project is Open Source to use, & any modifications must make it back to the GitHub repository of this project
 * For questions/comments/problems goto the GitHub page above, or my website: www.lveilleux.me/Chroma
 * Enjoy
 */

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created for helper functions to control and coordinate sending xml files into a single zip file of the given filename.
 */
public class ZipOutput {

    private ZipOutputStream zipFile;
    private static ZipOutput reference = null;

    /**
     * Private constructor to ensure this is a singleton object
     */
    private ZipOutput() {
        zipFile = null;
    }

    /**
     * Method used to get the reference to the current zip file object
     * for use in sending XML file outputs to.
     * @return - Reference to the ZipOutput instantiation.
     */
    protected static ZipOutput getReference() {
        if (reference == null)
            reference = new ZipOutput();
        return reference;
    }

    /**
     * Function to open a zipFile to later add XML files to.
     * @param fileName - Name of the zipFile to create
     * @throws FileNotFoundException - zipFile cannot be created with the given Name
     */
    public void createZipFile(String fileName) throws FileNotFoundException {
        zipFile = new ZipOutputStream(new FileOutputStream(fileName));
    }


    /**
     * Public function to output the resulting XML file directly into a zip file.
     * @param document - XML file to output to the .zip
     * @param fileName - fileName of the xml file.
     */
    public void outputToZipFile(Document document, String fileName) throws IOException {
        XMLOutputter xmlOutput = new XMLOutputter();
        // output xml
        xmlOutput.setFormat(Format.getPrettyFormat());
        FileOutputStream output = new FileOutputStream(fileName);
        xmlOutput.output(document, output);
        output.close();
        //Send XML to ZIP
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipFile.putNextEntry(zipEntry);
        File file = new File(fileName);
        if(!file.delete())
            System.out.println(fileName + " file was not deleted automatically.");
    }

    /**
     * Function to flush and close the zip file to finalize directory, before moving
     * the zip file to a .razerChroma file.
     */
    public void closeZipFile() throws IOException {
        zipFile.finish();
        zipFile.close();
    }

    /**
     * Method used to convert the file to the name given by the user, and move the file
     * to the desired output directory.
     * @param saveFile - Desired location and name of .razerchroma file
     */
    public void moveAndSwapFileExtension(File saveFile) {
        File zipFile = new File("Chroma.zip");
        int index = saveFile.toString().lastIndexOf('\\');
        String newName = saveFile.toString().substring(index + 1);
        if (!zipFile.renameTo(new File(newName))) {
            System.out.println("File was not able to be renamed. Please try again. \n\t" +
                    "(Or manually rename Chroma.zip in he project directory to Chroma.razerchroma)");
        }
        try {
            Files.move(new File(newName).toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to move file from project directory.\n");
            e.printStackTrace();
        }
    }

}
