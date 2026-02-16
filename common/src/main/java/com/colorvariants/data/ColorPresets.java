package com.colorvariants.data;

import com.colorvariants.core.ColorTransform;

import java.util.*;

/**
 * Built-in and custom color presets.
 */
public class ColorPresets {

    private static final Map<String, ColorTransform> BUILT_IN_PRESETS = new LinkedHashMap<>();
    private static final Map<String, ColorTransform> CUSTOM_PRESETS = new HashMap<>();

    static {
        initializeBuiltInPresets();
    }

    private static void initializeBuiltInPresets() {
        // Basic Colors
        BUILT_IN_PRESETS.put("preset.red", new ColorTransform(0, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.orange", new ColorTransform(30, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.yellow", new ColorTransform(60, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.green", new ColorTransform(120, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.cyan", new ColorTransform(180, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.blue", new ColorTransform(240, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.purple", new ColorTransform(280, 1.5f, 1.0f));
        BUILT_IN_PRESETS.put("preset.magenta", new ColorTransform(300, 1.5f, 1.0f));

        // Pastel Colors
        BUILT_IN_PRESETS.put("preset.pastel_pink", new ColorTransform(330, 0.5f, 1.2f));
        BUILT_IN_PRESETS.put("preset.pastel_blue", new ColorTransform(200, 0.4f, 1.3f));
        BUILT_IN_PRESETS.put("preset.pastel_green", new ColorTransform(140, 0.4f, 1.2f));
        BUILT_IN_PRESETS.put("preset.pastel_yellow", new ColorTransform(50, 0.5f, 1.3f));

        // Dark/Moody
        BUILT_IN_PRESETS.put("preset.dark_red", new ColorTransform(0, 1.2f, 0.6f));
        BUILT_IN_PRESETS.put("preset.dark_blue", new ColorTransform(240, 1.3f, 0.5f));
        BUILT_IN_PRESETS.put("preset.dark_green", new ColorTransform(120, 1.2f, 0.5f));
        BUILT_IN_PRESETS.put("preset.dark_purple", new ColorTransform(280, 1.3f, 0.6f));

        // Vibrant/Neon
        BUILT_IN_PRESETS.put("preset.neon_pink", new ColorTransform(320, 2.0f, 1.5f));
        BUILT_IN_PRESETS.put("preset.neon_blue", new ColorTransform(200, 2.0f, 1.4f));
        BUILT_IN_PRESETS.put("preset.neon_green", new ColorTransform(140, 2.0f, 1.5f));
        BUILT_IN_PRESETS.put("preset.neon_orange", new ColorTransform(20, 2.0f, 1.5f));

        // Metallic
        BUILT_IN_PRESETS.put("preset.gold", new ColorTransform(45, 1.8f, 1.3f));
        BUILT_IN_PRESETS.put("preset.silver", new ColorTransform(0, 0.1f, 1.4f));
        BUILT_IN_PRESETS.put("preset.bronze", new ColorTransform(30, 1.5f, 0.9f));
        BUILT_IN_PRESETS.put("preset.copper", new ColorTransform(20, 1.6f, 1.1f));

        // Natural/Earth Tones
        BUILT_IN_PRESETS.put("preset.forest", new ColorTransform(130, 1.3f, 0.7f));
        BUILT_IN_PRESETS.put("preset.desert", new ColorTransform(40, 1.2f, 0.9f));
        BUILT_IN_PRESETS.put("preset.ocean", new ColorTransform(200, 1.4f, 0.8f));
        BUILT_IN_PRESETS.put("preset.volcanic", new ColorTransform(10, 1.5f, 0.6f));

        // Special Effects
        BUILT_IN_PRESETS.put("preset.rainbow_red", new ColorTransform(0, 2.0f, 1.2f));
        BUILT_IN_PRESETS.put("preset.rainbow_orange", new ColorTransform(30, 2.0f, 1.2f));
        BUILT_IN_PRESETS.put("preset.rainbow_yellow", new ColorTransform(60, 2.0f, 1.2f));
        BUILT_IN_PRESETS.put("preset.rainbow_green", new ColorTransform(120, 2.0f, 1.2f));
        BUILT_IN_PRESETS.put("preset.rainbow_blue", new ColorTransform(240, 2.0f, 1.2f));
        BUILT_IN_PRESETS.put("preset.rainbow_purple", new ColorTransform(280, 2.0f, 1.2f));

        // Monochrome
        BUILT_IN_PRESETS.put("preset.grayscale", new ColorTransform(0, 0.0f, 1.0f));
        BUILT_IN_PRESETS.put("preset.brighten", new ColorTransform(0, 1.0f, 1.5f));
        BUILT_IN_PRESETS.put("preset.darken", new ColorTransform(0, 1.0f, 0.5f));
        BUILT_IN_PRESETS.put("preset.high_contrast", new ColorTransform(0, 1.8f, 1.3f));
    }

    /**
     * Gets all available preset names (built-in + custom).
     */
    public static List<String> getPresetNames() {
        List<String> names = new ArrayList<>();
        names.addAll(BUILT_IN_PRESETS.keySet());
        names.addAll(CUSTOM_PRESETS.keySet());
        return names;
    }

    /**
     * Gets all built-in preset names.
     */
    public static List<String> getBuiltInPresetNames() {
        return new ArrayList<>(BUILT_IN_PRESETS.keySet());
    }

    /**
     * Gets all custom preset names.
     */
    public static List<String> getCustomPresetNames() {
        return new ArrayList<>(CUSTOM_PRESETS.keySet());
    }

    /**
     * Gets a preset by name.
     */
    public static ColorTransform getPreset(String name) {
        ColorTransform preset = BUILT_IN_PRESETS.get(name);
        if (preset == null) {
            preset = CUSTOM_PRESETS.get(name);
        }
        return preset != null ? preset : ColorTransform.NONE;
    }

    /**
     * Saves a custom preset.
     */
    public static void saveCustomPreset(String name, ColorTransform transform) {
        CUSTOM_PRESETS.put(name, transform);
    }

    /**
     * Deletes a custom preset.
     */
    public static boolean deleteCustomPreset(String name) {
        return CUSTOM_PRESETS.remove(name) != null;
    }

    /**
     * Checks if a preset exists.
     */
    public static boolean hasPreset(String name) {
        return BUILT_IN_PRESETS.containsKey(name) || CUSTOM_PRESETS.containsKey(name);
    }

    /**
     * Checks if a preset is built-in.
     */
    public static boolean isBuiltIn(String name) {
        return BUILT_IN_PRESETS.containsKey(name);
    }

    /**
     * Gets presets by category.
     */
    public static Map<String, List<String>> getPresetsByCategory() {
        Map<String, List<String>> categories = new LinkedHashMap<>();

        categories.put("category.basic", Arrays.asList(
                "preset.red", "preset.orange", "preset.yellow", "preset.green",
                "preset.cyan", "preset.blue", "preset.purple", "preset.magenta"));

        categories.put("category.pastel", Arrays.asList(
                "preset.pastel_pink", "preset.pastel_blue",
                "preset.pastel_green", "preset.pastel_yellow"));

        categories.put("category.dark", Arrays.asList(
                "preset.dark_red", "preset.dark_blue",
                "preset.dark_green", "preset.dark_purple"));

        categories.put("category.neon", Arrays.asList(
                "preset.neon_pink", "preset.neon_blue",
                "preset.neon_green", "preset.neon_orange"));

        categories.put("category.metallic", Arrays.asList(
                "preset.gold", "preset.silver", "preset.bronze", "preset.copper"));

        categories.put("category.natural", Arrays.asList(
                "preset.forest", "preset.desert", "preset.ocean", "preset.volcanic"));

        categories.put("category.rainbow", Arrays.asList(
                "preset.rainbow_red", "preset.rainbow_orange", "preset.rainbow_yellow",
                "preset.rainbow_green", "preset.rainbow_blue", "preset.rainbow_purple"));

        categories.put("category.effects", Arrays.asList(
                "preset.grayscale", "preset.brighten",
                "preset.darken", "preset.high_contrast"));

        if (!CUSTOM_PRESETS.isEmpty()) {
            categories.put("category.custom", new ArrayList<>(CUSTOM_PRESETS.keySet()));
        }

        return categories;
    }
}
