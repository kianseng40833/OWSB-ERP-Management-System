package Management.Controller;

import Management.Model.User;
import Management.Model.UserRole;
import Management.FileIO.FileIO;

import java.util.ArrayList;
import java.util.List;

public class RegisterController {
    private final String rootFilePath = "src/main/resources/";
    private final String userFilePath = "users.txt";
    private final String roleFilePath = "userRole.txt";

    // Load roles from file
    public List<UserRole> loadRoles() {
        List<String> roleData = FileIO.readFromFile(rootFilePath + roleFilePath, Integer.MAX_VALUE);
        List<UserRole> roles = new ArrayList<>();

        for (int i = 1; i < roleData.size(); i++) { // start from index 1 to skip header
            UserRole userRole = UserRole.fromFileString(roleData.get(i));
            if (userRole != null) {
                roles.add(userRole);
            }
        }
        return roles;
    }

    private String generateNewUserId() {
        List<String> existingUsers = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        return "u_id" + (existingUsers.size() + 1);
    }

    // New method to check if the username already exists
    public boolean isUsernameTaken(String username) {
        List<String> existingUsersData = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        for (String userData : existingUsersData) {
            String[] parts = userData.split(",");
            if (parts.length > 1 && parts[1].trim().equalsIgnoreCase(username)) {
                return true; // Username already exists
            }
        }
        return false; // Username is available
    }

    public boolean registerUser(String username, String password, String userRoleId, String email, String emailPassword, String phoneNumber) {
        // 1. Check if username is taken
        if (isUsernameTaken(username)) {
            System.out.println("❌ Username already exists: " + username);
            return false; // Username already exists, so registration fails
        }

        // 2. Load all roles from role file
        List<UserRole> roles = loadRoles();
        boolean isValidRole = false;

        // Validate the role against available roles
        for (UserRole role : roles) {
            if (role.getUserRoleId().equalsIgnoreCase(userRoleId.trim())) {
                isValidRole = true;
                break;
            }
        }

        if (!isValidRole) {
            System.out.println("❌ Invalid role ID: " + userRoleId);
            return false; // Invalid role
        }

        // 3. Generate userId (you might use a UUID or an auto-incremented ID logic)
        String userId = generateNewUserId();
        User user = new User(userId, username, password, userRoleId, email, emailPassword, phoneNumber);
        String userData = user.toFileString();
        FileIO.writeToFile(rootFilePath + userFilePath, List.of(userData), true);

        System.out.println("✅ User registered successfully.");
        return true;
    }
}
