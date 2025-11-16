package acainfo.back.domain.validation;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[(]?[0-9]{1,4}[)]?[-\\s\\.]?[0-9]{1,9}$"
    );

    private ValidationUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return true; // Phone is optional
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // At least one digit, one lowercase, one uppercase, one special char
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
    }

    public static void requireNonNull(Object object, String parameterName) {
        if (object == null) {
            throw new IllegalArgumentException(parameterName + " cannot be null");
        }
    }

    public static void requireNonBlank(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(parameterName + " cannot be null or blank");
        }
    }
}
