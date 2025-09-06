package Management.Model;

public class LoggedInUser {
    private static User currentUser;

    // Private constructor to prevent instantiation
    private LoggedInUser() {}

    // Set the current user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Get the current user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Check if a user is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Clear the current user (log out)
    public static void logout() {
        currentUser = null;
    }
}
