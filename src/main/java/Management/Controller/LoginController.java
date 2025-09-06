package Management.Controller;

import Management.Model.User;
import Management.FileIO.FileIO;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class LoginController {

    private User authenticatedUser;
    private final String rootFilePath = "src/main/resources/";
    private final String userFilePath = "users.txt";

    public boolean login(String username, String password) {
        List<String> userData = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        
        for (String line : userData) {
            String[] userFields = line.split(",");
            if (userFields.length == 7) {
                String fileUserID = userFields[0].trim();
                String fileUsername = userFields[1].trim();
                String filePassword = userFields[2].trim();
                String fileUserRoleId = userFields[3].trim();
                String fileEmail = userFields[4].trim();
                String fileEmailPassword = userFields[5].trim();
                String filePhoneNumber = userFields[6].trim();

                if (fileUsername.equals(username) && filePassword.equals(password)) {
                    authenticatedUser = new User(fileUserID, fileUsername, filePassword, fileUserRoleId, 
                                               fileEmail, fileEmailPassword, filePhoneNumber);
                    return true;
                }
            } else {
                System.err.println("Skipping invalid user data: " + line);
                JOptionPane.showMessageDialog(null, "Invalid user data found in users.txt: " + line, 
                                             "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
        return false;
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public boolean initiatePasswordReset(String emailOrUsername) {
        List<String> userData = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        for (String line : userData) {
            String[] userFields = line.split(",");
            if (userFields.length == 7) {
                String fileUsername = userFields[1].trim();
                String fileEmail = userFields[4].trim();

                if (fileUsername.equals(emailOrUsername) || fileEmail.equals(emailOrUsername)) {
                    System.out.println("Reset email sent to " + fileEmail);
                    return true;
                }
            }
        }
        return false;
    }

    public User getUserByEmail(String email) {
        List<String> userData = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        
        for (String line : userData) {
            String[] userFields = line.split(",");
            if (userFields.length == 7) {
                String fileEmail = userFields[4].trim();
                
                if (fileEmail.equalsIgnoreCase(email)) {
                    return new User(
                        userFields[0].trim(),  // userID
                        userFields[1].trim(),  // username
                        userFields[2].trim(),  // password
                        userFields[3].trim(),  // userRoleId
                        fileEmail,            // email
                        userFields[5].trim(),  // emailPassword
                        userFields[6].trim()   // phoneNumber
                    );
                }
            }
        }
        return null;
    }

    public boolean resetPassword(String username, String newPassword) {
    List<String> userData = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
    List<String> updatedUserData = new ArrayList<>();
    boolean userFound = false;
    
    for (String line : userData) {
        String[] userFields = line.split(",");
        if (userFields.length == 7) {
            String fileUsername = userFields[1].trim();
            
            if (fileUsername.equals(username)) {
                // Update only the password field (index 2)
                userFields[2] = newPassword;
                userFound = true;
            }
            // Reconstruct the line with all original data (only password changed)
            updatedUserData.add(String.join(",", userFields));
        }
    }
    
    if (userFound) {
        // Write all users back to the file
        return FileIO.writeToFile(rootFilePath + userFilePath, updatedUserData, false);
    }
    
    return false;
}
}