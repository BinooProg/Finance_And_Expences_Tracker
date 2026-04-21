package service;

import model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class UserService {
    private static final String FILE_PATH = "src/main/resources/data/users.txt";

    public User getUserByEmail(String email) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH), StandardCharsets.UTF_8);

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length < 5) {
                    continue;
                }

                int id = Integer.parseInt(parts[0].trim());
                String firstName = parts[1].trim();
                String lastName = parts[2].trim();
                String storedEmail = parts[3].trim();
                String passwordHash = parts[4].trim();

                if (storedEmail.equalsIgnoreCase(email.trim())) {
                    return new User(id, firstName, lastName, storedEmail, passwordHash);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}