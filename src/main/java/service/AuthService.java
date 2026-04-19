package service;

import java.io.*;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import util.HashUtil;

public class AuthService {
    private static final String FILE_PATH = "src/main/resources/data/users.txt";

    /**
     * Registers a new user by validating input, checking email uniqueness, hashing the password,
     * and appending the user record to the users file.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param rawPassword the user's plain-text password before hashing
     * @return true when the user record is created successfully
     * @throws IllegalArgumentException if any required input is blank or the email already exists
     * @throws UncheckedIOException if writing user data fails
     */
    public boolean createUser(String firstName, String lastName, String email, String rawPassword) {
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("First name, last name, email and password are required");
        }
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashed = HashUtil.md5(rawPassword);
        int nextId = getNextId();
        String row = String.format("%d|%s|%s|%s|%s", nextId, firstName.trim(), lastName.trim(), email.trim(), hashed);

        try {
            ensureFileExists();
            Files.writeString(getFilePath(), row + System.lineSeparator(), StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to persist user data", e);
        }
    }

    /**
     * Validates user login credentials against stored user records.
     *
     * @param email the email to authenticate
     * @param rawPassword the plain-text password to verify
     * @return true when email exists and password hash matches; false when credentials do not match
     * @throws IllegalArgumentException if email or password is blank
     * @throws UncheckedIOException if reading user data fails
     */
    public boolean validateUser(String email, String rawPassword) {
        if (isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String inputHash = HashUtil.md5(rawPassword);

        try {
            Path path = getFilePath();
            if (!Files.exists(path)) {
                return false;
            }

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("\\|");
                if (parts.length < 5) {
                    continue;
                }
                String storedEmail = parts[3].trim();
                String storedHash = parts[4].trim();
                if (storedEmail.equalsIgnoreCase(email.trim())) {
                    return storedHash.equals(inputHash);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read user data", e);
        }
        return false;
    }

    /**
     * Checks whether a user with the provided email already exists.
     *
     * @param email the email to search for
     * @return true if a matching email is found; false otherwise
     * @throws UncheckedIOException if reading user data fails
     */
    private boolean emailExists(String email) {
        try {
            Path path = getFilePath();
            if (!Files.exists(path)) {
                return false;
            }
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4 && parts[3].trim().equalsIgnoreCase(email.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to check email existence", e);
        }
        return false;
    }


    /**
     * Calculates the next available user ID based on the maximum existing ID in the users file.
     *
     * @return the next sequential user ID starting from 1
     * @throws UncheckedIOException if reading user data fails
     */
    private int getNextId() {
        int maxId = 0;
        try {
            Path path = getFilePath();
            if (!Files.exists(path)) {
                return 1;
            }
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length > 0) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        if (id > maxId) {
                            maxId = id;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate next user id", e);
        }
        return maxId + 1;
    }

    /**
     * Ensures that the users file and its parent directory exist.
     *
     * @throws IOException if directory or file creation fails
     */
    private void ensureFileExists() throws IOException {
        Path path = getFilePath();
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    /**
     * Resolves and returns the filesystem path of the users data file.
     *
     * @return the path to users.txt
     */
    private Path getFilePath() {
        return Paths.get(FILE_PATH);
    }

    /**
     * Checks whether a text value is null, empty, or whitespace-only.
     *
     * @param value the value to evaluate
     * @return true if the value is blank; false otherwise
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }


}
