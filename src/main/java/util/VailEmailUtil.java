package util;

import java.util.regex.Pattern;

public class VailEmailUtil {
    /**
     * Validates whether an email string matches a common email format.
     *
     * @param email the email to validate
     * @return true if email is non-blank and matches the expected format; false otherwise
     */
    public static boolean isValidEmailFormat(String email) {
        if (email == null) {
            return false;
        }
        String normalized = email.trim();
        if (normalized.isEmpty()) {
            return false;
        }
        return Pattern.compile(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        ).matcher(normalized).matches();
    }

}
