package service;

public final class SessionManager {
    private static String loggedInUserEmail;

    private SessionManager() {
    }

    public static void setLoggedInUserEmail(String email) {
        loggedInUserEmail = email;
    }

    public static String getLoggedInUserEmail() {
        return loggedInUserEmail;
    }

    public static void clearSession() {
        loggedInUserEmail = null;
    }
}