package Management.FileIO;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileIO {

//     Write data to file (overwrite or append)
    public static boolean writeToFile(String filePath, List<String> data, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            for (String line : data) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
        return true;
    }

    // Read specific number of lines from file
    public static List<String> readFromFile(String filePath, int numberOfItems) {
        List<String> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < numberOfItems) {
                data.add(line);
                count++;
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return data;
    }

    // Edit a specific line (by index)
    public static boolean editLines(String filePath, List<String> newLines, boolean append) {
        try {
            writeToFile(filePath, newLines, append);
            return true;
        } catch (Exception e) {
            System.out.println("Error writing lines: " + e.getMessage());
            return false;
        }
    }


    // Delete a specific line (by index)
    public static boolean deleteLine(String filePath, List<String> newLines, boolean append) {
        return writeToFile(filePath, newLines, append);
    }

    // Read all lines (helper function)
    private static List<String> readAllLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    // ✅ Picture copy utility (from source path to resource path)
    public static void copyImageToResources(String sourceImagePath, String destinationDirPath) {
        try {
            File sourceFile = new File(sourceImagePath);
            if (!sourceFile.exists()) {
                System.out.println("Source image does not exist: " + sourceImagePath);
                return;
            }

            File destinationDir = new File(destinationDirPath);
            if (!destinationDir.exists()) {
                destinationDir.mkdirs(); // Create the folder if it doesn't exist
            }

            Path sourcePath = sourceFile.toPath();
            Path destinationPath = Paths.get(destinationDirPath, sourceFile.getName());

            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image copied to: " + destinationPath);
        } catch (IOException e) {
            System.out.println("Error copying image: " + e.getMessage());
        }
    }
}
