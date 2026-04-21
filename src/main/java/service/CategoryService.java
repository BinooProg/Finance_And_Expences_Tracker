package service;

import model.Category;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {
    private static final String FILE_PATH = "src/main/resources/data/categories.txt";

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);

        try {
            ensureFileExists();

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

        String trimmedName = name.trim();
        List<Category> categories = getAllCategories();

        for (Category c : categories) {
            if (c.getName().equalsIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("Category already exists.");
            }
        }

        int nextId = getNextId();
        String row = nextId + "|" + trimmedName;

        try {
            ensureFileExists();
            Files.writeString(
                    Paths.get(FILE_PATH),
                    row + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to save category.", e);
        }
    }

    public void updateCategory(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        String trimmedName = newName.trim();
        List<Category> categories = getAllCategories();
        boolean found = false;

        for (Category category : categories) {
            if (category.getId() != id && category.getName().equalsIgnoreCase(trimmedName)) {
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

        rewriteAll(categories);
    }

    public void deleteCategory(int id) {
        List<Category> categories = getAllCategories();
        boolean removed = categories.removeIf(category -> category.getId() == id);

        if (!removed) {
            throw new IllegalArgumentException("Category not found.");
        }

        rewriteAll(categories);
    }

    private void rewriteAll(List<Category> categories) {
        List<String> rows = new ArrayList<>();
        for (Category c : categories) {
            rows.add(c.getId() + "|" + c.getName());
        }

        try {
            Files.write(
                    Paths.get(FILE_PATH),
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

    private void ensureFileExists() throws IOException {
        Path path = Paths.get(FILE_PATH);
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }
}