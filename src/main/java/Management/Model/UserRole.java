package Management.Model;

public class UserRole {
    private String userRoleId;
    private String roleName;

    public UserRole(String userRoleId, String roleName) {
        this.userRoleId = userRoleId;
        this.roleName = roleName;
    }

    public String getUserRoleId() {
        return userRoleId;
    }

    public String getRoleName() {
        return roleName;
    }

    @Override
    public String toString() { return roleName; }

    public static UserRole fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 2) {
            return new UserRole(parts[0].trim(), parts[1].trim());
        }
        return null;
    }
}
