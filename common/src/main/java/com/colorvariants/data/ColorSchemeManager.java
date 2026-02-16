package com.colorvariants.data;

import com.google.gson.*;
import com.colorvariants.core.ColorTransform;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Handles importing and exporting color schemes to/from JSON files.
 */
public class ColorSchemeManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColorSchemeManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Represents a color scheme that can be saved and loaded.
     */
    public static class ColorScheme {
        private String name;
        private String description;
        private String author;
        private long createdDate;
        private Map<String, ColorEntry> colors;

        public ColorScheme() {
            this.colors = new HashMap<>();
            this.createdDate = System.currentTimeMillis();
        }

        public ColorScheme(String name, String description, String author) {
            this();
            this.name = name;
            this.description = description;
            this.author = author;
        }

        public void addColor(BlockPos pos, ColorTransform transform) {
            String key = posToString(pos);
            colors.put(key, new ColorEntry(transform));
        }

        public Map<BlockPos, ColorTransform> getColors() {
            Map<BlockPos, ColorTransform> result = new HashMap<>();
            for (Map.Entry<String, ColorEntry> entry : colors.entrySet()) {
                BlockPos pos = stringToPos(entry.getKey());
                if (pos != null) {
                    result.put(pos, entry.getValue().toTransform());
                }
            }
            return result;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getAuthor() {
            return author;
        }

        public long getCreatedDate() {
            return createdDate;
        }

        public int getColorCount() {
            return colors.size();
        }
    }

    /**
     * Internal representation of a color entry.
     */
    private static class ColorEntry {
        private float hue;
        private float saturation;
        private float brightness;

        public ColorEntry() {
        }

        public ColorEntry(ColorTransform transform) {
            this.hue = transform.getHueShift();
            this.saturation = transform.getSaturation();
            this.brightness = transform.getBrightness();
        }

        public ColorTransform toTransform() {
            return new ColorTransform(hue, saturation, brightness);
        }
    }

    /**
     * Exports a color scheme to a JSON file.
     */
    public static boolean exportScheme(ColorScheme scheme, Path filePath) {
        try {
            String json = GSON.toJson(scheme);
            Files.writeString(filePath, json);
            LOGGER.info("Exported color scheme to: {}", filePath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to export color scheme", e);
            return false;
        }
    }

    /**
     * Imports a color scheme from a JSON file.
     */
    public static ColorScheme importScheme(Path filePath) {
        try {
            String json = Files.readString(filePath);
            ColorScheme scheme = GSON.fromJson(json, ColorScheme.class);
            LOGGER.info("Imported color scheme from: {}", filePath);
            return scheme;
        } catch (IOException e) {
            LOGGER.error("Failed to import color scheme", e);
            return null;
        }
    }

    /**
     * Exports just the color palette (no positions).
     */
    public static boolean exportPalette(List<ColorTransform> colors, Path filePath, String name) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("type", "palette");
            json.addProperty("version", "1.0");

            JsonArray colorsArray = new JsonArray();
            for (ColorTransform transform : colors) {
                JsonObject colorObj = new JsonObject();
                colorObj.addProperty("hue", transform.getHueShift());
                colorObj.addProperty("saturation", transform.getSaturation());
                colorObj.addProperty("brightness", transform.getBrightness());
                colorsArray.add(colorObj);
            }
            json.add("colors", colorsArray);

            String jsonString = GSON.toJson(json);
            Files.writeString(filePath, jsonString);
            LOGGER.info("Exported palette to: {}", filePath);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to export palette", e);
            return false;
        }
    }

    /**
     * Imports a color palette from a JSON file.
     */
    public static List<ColorTransform> importPalette(Path filePath) {
        try {
            String json = Files.readString(filePath);
            JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();

            List<ColorTransform> colors = new ArrayList<>();
            JsonArray colorsArray = jsonObj.getAsJsonArray("colors");

            for (JsonElement element : colorsArray) {
                JsonObject colorObj = element.getAsJsonObject();
                float hue = colorObj.get("hue").getAsFloat();
                float saturation = colorObj.get("saturation").getAsFloat();
                float brightness = colorObj.get("brightness").getAsFloat();
                colors.add(new ColorTransform(hue, saturation, brightness));
            }

            LOGGER.info("Imported {} colors from palette", colors.size());
            return colors;
        } catch (IOException e) {
            LOGGER.error("Failed to import palette", e);
            return new ArrayList<>();
        }
    }

    /**
     * Validates a color scheme file.
     */
    public static boolean validateScheme(Path filePath) {
        try {
            ColorScheme scheme = importScheme(filePath);
            return scheme != null && scheme.colors != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets a list of available scheme files in a directory.
     */
    public static List<Path> listSchemes(Path directory) {
        List<Path> schemes = new ArrayList<>();

        try {
            if (Files.exists(directory) && Files.isDirectory(directory)) {
                Files.walk(directory, 1)
                        .filter(path -> path.toString().endsWith(".json"))
                        .filter(ColorSchemeManager::validateScheme)
                        .forEach(schemes::add);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list schemes", e);
        }

        return schemes;
    }

    // Helper methods
    private static String posToString(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static BlockPos stringToPos(String str) {
        try {
            String[] parts = str.split(",");
            return new BlockPos(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        } catch (Exception e) {
            LOGGER.warn("Failed to parse position: {}", str);
            return null;
        }
    }
}
