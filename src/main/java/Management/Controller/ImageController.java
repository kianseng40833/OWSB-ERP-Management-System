package Management.Controller;

import java.io.IOException;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ImageController {
    private static final String ROOT_PATH = "src/main/resources/";
    private static final String IMAGE_FOLDER = "Itemimage/";

    public static String processImagePath(String originalPath, String itemId) throws IOException {
        if (originalPath == null || originalPath.isEmpty()) {
            return "";
        }

        // Check if path is already in correct format
        String expectedName = itemId + getFileExtension(originalPath);
        if (originalPath.startsWith(IMAGE_FOLDER) &&
                originalPath.endsWith(expectedName)) {
            return originalPath;
        }

        // Handle relative paths (just filenames)
        if (!Paths.get(originalPath).isAbsolute()) {
            originalPath = ROOT_PATH + IMAGE_FOLDER + originalPath;
        }

        Path sourcePath = Paths.get(originalPath);
        if (!Files.exists(sourcePath)) {
            throw new IOException("Source image file doesn't exist: " + originalPath);
        }

        // Get the file extension from original file
        String extension = getFileExtension(originalPath);
        String newFileName = itemId + extension;
        Path targetPath = Paths.get(ROOT_PATH + IMAGE_FOLDER + newFileName);

        // Create directory if it doesn't exist
        Files.createDirectories(targetPath.getParent());

        // Read and rewrite the image to ensure proper format
        try {
            BufferedImage image = ImageIO.read(sourcePath.toFile());
            if (image == null) {
                throw new IOException("Unsupported image format");
            }

            // Write with new filename
            String formatName = extension.substring(1); // remove the dot
            ImageIO.write(image, formatName, targetPath.toFile());

            return IMAGE_FOLDER + newFileName;
        } catch (IOException e) {
            throw new IOException("Failed to process image: " + e.getMessage());
        }
    }

    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot).toLowerCase();
        }
        return ".png"; // default extension
    }

    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || !imagePath.startsWith(IMAGE_FOLDER)) {
            return false;
        }

        try {
            Path path = Paths.get(ROOT_PATH + imagePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error deleting image: " + e.getMessage());
            return false;
        }
    }
}