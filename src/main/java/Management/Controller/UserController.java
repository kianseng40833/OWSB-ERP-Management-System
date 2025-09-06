package Management.Controller;

import Management.Model.PurchaseRequisition;
import Management.Model.User;
import Management.FileIO.FileIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserController {
    private final String rootFilePath = "src/main/resources/";
    private final String userFilePath = "users.txt";
    private final String roleFilePath = "userRole.txt";

    private List<User> userList = new ArrayList<>();

    // Load all user roles into a map (roleId -> roleName)
    public HashMap<String, String> loadRoleMap() {
        HashMap<String, String> map = new HashMap<>();
        List<String> roleLines = FileIO.readFromFile(rootFilePath + roleFilePath, Integer.MAX_VALUE);
        for (String line : roleLines) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                map.put(parts[0].trim(), parts[1].trim());
            }
        }
        return map;
    }

    public boolean addUser(User user) {
        userList.add(user);
        FileIO.writeToFile(rootFilePath + userFilePath, List.of(user.toFileString()), true);
        return true;
    }

    // Load all users from the file
    public List<User> loadUsers() {
        List<String> data = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        userList.clear();

        for (int i = 1; i < data.size(); i++) {
            User users = User.fromFileString(data.get(i));
            if (users != null) {
                userList.add(users);
            }
        }

        return userList;
    }

    // Update a user in the file
    public boolean updateUserInFile(User updatedUser) {
        List<String> lines = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        boolean updated = false;

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            if (parts.length >= 1 && parts[0].trim().equals(updatedUser.getUserId())) {
                lines.set(i, updatedUser.toFileString());
                updated = true;
                break;
            }
        }

        if (updated) {
            return FileIO.editLines(rootFilePath + userFilePath, lines, false);
        }

        return false;
    }

    public boolean deleteFile(User user) {
        List<String> lines = FileIO.readFromFile(rootFilePath + userFilePath, Integer.MAX_VALUE);
        boolean deleted = false;

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            if (parts.length >= 1 && parts[0].trim().equals(user.getUserId())) {
                lines.remove(i); // 🗑️ Remove the line
                deleted = true;
                break;
            }
        }

        if (deleted) {
            return FileIO.writeToFile(rootFilePath + userFilePath, lines, false);
        }

        return false;
    }

    // Optional: Method to find a user by ID
    public User getUserById(String userId) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }
}