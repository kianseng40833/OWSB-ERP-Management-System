package Management.Model;

public class User {
    private String userId;
    private String username;
    private String password;
    private String userRoleId;
    private String email;
    private String emailPassword; // Added emailPassword field
    private String phoneNumber;

    // Updated constructor to include emailPassword
    public User(String userId, String username, String password, String userRoleId, String email, String emailPassword, String phoneNumber) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.userRoleId = userRoleId;
        this.email = email;
        this.emailPassword = emailPassword; // Initialize emailPassword
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getUserRoleId() { return userRoleId; }
    public String getEmail() { return email; }
    public String getEmailPassword() { return emailPassword; } // Getter for emailPassword
    public String getPhoneNumber() { return phoneNumber; }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserRoleId(String userRoleId) {
        this.userRoleId = userRoleId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Updated toFileString() to include emailPassword
    public String toFileString() {
        return String.join(",", userId, username, password, userRoleId, email, emailPassword, phoneNumber);
    }

    // Updated fromFileString() to parse emailPassword
    public static User fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 7) { // Now expecting 7 parts
            System.err.println("Invalid data format: " + line);
            return null;   // Return null if the data is invalid
        }
        return new User(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim(), parts[6].trim());
    }
}