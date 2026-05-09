package service;

import model.Category;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryService {
    private static final String CATEGORIES_DIRECTORY = "src/main/resources/data/categories";

    private final List<Category> categories = new ArrayList<>();
    private Integer loadedUserId;
    private final UserService userService = new UserService();

    public List<Category> getAllCategories() {
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            categories.clear();
            loadedUserId = null;
            return categories;
        }

        if (loadedUserId == null || !loadedUserId.equals(currentUserId)) {
            categories.clear();
            loadedUserId = currentUserId;
        }

        if (!categories.isEmpty()) {
            return categories;
        }

        Path path = getUserCategoriesPath(currentUserId);

        try {
            ensureFileExists(path);

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length < 2) {
                    continue;
                }

                int id = Integer.parseInt(parts[0].trim());
                String name = parts[1].trim();

                categories.add(new Category(id, name));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return categories;
    }

    public void addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No logged-in user found.");
        }

        getAllCategories();

        String trimmedName = name.trim();
        String normalizedInput = normalizeName(trimmedName);

        for (Category c : categories) {
            String normalizedExistingName = normalizeName(c.getName());
            if (normalizedExistingName.equals(normalizedInput)) {
                throw new IllegalArgumentException("Category already exists.");
            }
        }

        int nextId = getNextId();
        String row = nextId + "|" + trimmedName;

        try {
            Path path = getUserCategoriesPath(currentUserId);
            ensureFileExists(path);
            Files.writeString(
                    path,
                    row + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
            );
            categories.add(new Category(nextId, trimmedName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save category.", e);
        }
    }

    public void updateCategory(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        getAllCategories();

        String trimmedName = newName.trim();
        String normalizedInput = normalizeName(trimmedName);
        boolean found = false;

        for (Category category : categories) {
            String normalizedExistingName = normalizeName(category.getName());
            if (category.getId() != id && normalizedExistingName.equals(normalizedInput)) {
                throw new IllegalArgumentException("Category already exists.");
            }
        }

        for (Category category : categories) {
            if (category.getId() == id) {
                category.setName(trimmedName);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Category not found.");
        }

        rewriteAll();
    }

    public void deleteCategory(int id) {
        getAllCategories();
        boolean removed = categories.removeIf(category -> category.getId() == id);

        if (!removed) {
            throw new IllegalArgumentException("Category not found.");
        }

        rewriteAll();
    }

    private void rewriteAll() {
        List<String> rows = new ArrayList<>();
        for (Category c : categories) {
            rows.add(c.getId() + "|" + c.getName());
        }

        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                throw new IllegalStateException("No logged-in user found.");
            }
            Files.write(
                    getUserCategoriesPath(currentUserId),
                    rows,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to update categories.", e);
        }
    }

    private int getNextId() {
        int max = 0;
        for (Category c : getAllCategories()) {
            if (c.getId() > max) {
                max = c.getId();
            }
        }
        return max + 1;
    }

    private Integer getCurrentUserId() {
        String email = SessionManager.getLoggedInUserEmail();
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        var user = userService.getUserByEmail(email);
        return user == null ? null : user.getId();
    }

    private Path getUserCategoriesPath(int userId) {
        return Paths.get(CATEGORIES_DIRECTORY, userId + "_categories.txt");
    }

    private void ensureFileExists(Path path) throws IOException {
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
