package com.colorvariants.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.data.ColorPresets;
import com.colorvariants.network.ColorUpdatePacket;
import com.colorvariants.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enhanced color picker with presets, favorites, and eyedropper.
 */
public class EnhancedColorPickerScreen extends Screen {

    private final BlockPos pos;
    private final BlockState state;

    private SliderWidget hueSlider;
    private SliderWidget saturationSlider;
    private SliderWidget brightnessSlider;

    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;

    // Buttons
    private Button applyButton;
    private Button resetButton;
    private Button cancelButton;
    private Button savePresetButton;
    private Button eyedropperButton;

    // Preset selection
    private int selectedPresetIndex = -1;
    private List<String> presetNames;
    private String selectedCategory = "category.basic";

    // UI Layout
    private static final int PRESET_BUTTON_SIZE = 24;
    private static final int PRESET_COLUMNS = 4;
    private int presetScrollOffset = 0;

    public EnhancedColorPickerScreen(BlockPos pos, BlockState state) {
        super(Component.translatable("gui.colorvariants.enhanced_color_picker.title"));
        this.pos = pos;
        this.state = state;
        loadPresets();
    }

    private void loadPresets() {
        Map<String, List<String>> categories = ColorPresets.getPresetsByCategory();
        presetNames = categories.getOrDefault(selectedCategory, new ArrayList<>());
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Main sliders
        int sliderWidth = 200;
        int sliderX = centerX - sliderWidth / 2;
        int startY = centerY - 60;

        hueSlider = new SliderWidget(
                sliderX, startY, sliderWidth, 20,
                Component.translatable("gui.colorvariants.hue"),
                0, 360, hue,
                value -> {
                    hue = value;
                    updatePreview();
                });
        addRenderableWidget(hueSlider);

        saturationSlider = new SliderWidget(
                sliderX, startY + 25, sliderWidth, 20,
                Component.translatable("gui.colorvariants.saturation"),
                0, 2, saturation,
                value -> {
                    saturation = value;
                    updatePreview();
                });
        addRenderableWidget(saturationSlider);

        brightnessSlider = new SliderWidget(
                sliderX, startY + 50, sliderWidth, 20,
                Component.translatable("gui.colorvariants.brightness"),
                0, 2, brightness,
                value -> {
                    brightness = value;
                    updatePreview();
                });
        addRenderableWidget(brightnessSlider);

        // Action buttons
        int buttonY = startY + 85;
        int buttonWidth = 60;

        applyButton = Button.builder(
                Component.translatable("gui.colorvariants.apply"),
                btn -> applyColor()).bounds(sliderX, buttonY, buttonWidth, 20).build();
        addRenderableWidget(applyButton);

        resetButton = Button.builder(
                Component.translatable("gui.colorvariants.reset"),
                btn -> resetColor()).bounds(sliderX + buttonWidth + 5, buttonY, buttonWidth, 20).build();
        addRenderableWidget(resetButton);

        cancelButton = Button.builder(
                Component.translatable("gui.colorvariants.cancel"),
                btn -> onClose()).bounds(sliderX + (buttonWidth + 5) * 2, buttonY, buttonWidth, 20).build();
        addRenderableWidget(cancelButton);

        // Preset save button
        savePresetButton = Button.builder(
                Component.literal("💾"),
                btn -> saveCustomPreset()).bounds(sliderX + sliderWidth - 25, startY - 30, 25, 20).build();
        addRenderableWidget(savePresetButton);

        // Eyedropper button
        eyedropperButton = Button.builder(
                Component.literal("🎨"),
                btn -> activateEyedropper()).bounds(sliderX - 30, startY, 25, 20).build();
        addRenderableWidget(eyedropperButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);

        // Title
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        // Render preset buttons
        renderPresets(graphics, mouseX, mouseY);

        // Render current color preview
        renderColorPreview(graphics);

        // Render current values
        int centerX = width / 2;
        int centerY = height / 2;
        graphics.drawString(font,
                String.format("H: %.0f°  S: %.2f  B: %.2f", hue, saturation, brightness),
                centerX - 100, centerY + 50, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void renderPresets(GuiGraphics graphics, int mouseX, int mouseY) {
        int startX = 10;
        int startY = 60;

        graphics.drawString(font, Component.translatable("gui.colorvariants.presets"),
                startX, startY - 15, 0xFFFFFF);

        for (int i = 0; i < Math.min(presetNames.size(), 12); i++) {
            String presetName = presetNames.get(i + presetScrollOffset);
            ColorTransform preset = ColorPresets.getPreset(presetName);

            int col = i % PRESET_COLUMNS;
            int row = i / PRESET_COLUMNS;
            int x = startX + col * (PRESET_BUTTON_SIZE + 4);
            int y = startY + row * (PRESET_BUTTON_SIZE + 4);

            // Get preview color
            int color = getPreviewColor(preset);

            // Draw preset button
            graphics.fill(x, y, x + PRESET_BUTTON_SIZE, y + PRESET_BUTTON_SIZE, 0xFF000000);
            graphics.fill(x + 1, y + 1, x + PRESET_BUTTON_SIZE - 1, y + PRESET_BUTTON_SIZE - 1, color);

            // Highlight selected
            if (i == selectedPresetIndex) {
                graphics.fill(x - 1, y - 1, x + PRESET_BUTTON_SIZE + 1, y, 0xFFFFFFFF);
                graphics.fill(x - 1, y, x, y + PRESET_BUTTON_SIZE, 0xFFFFFFFF);
                graphics.fill(x + PRESET_BUTTON_SIZE, y, x + PRESET_BUTTON_SIZE + 1, y + PRESET_BUTTON_SIZE + 1,
                        0xFFFFFFFF);
                graphics.fill(x, y + PRESET_BUTTON_SIZE, x + PRESET_BUTTON_SIZE, y + PRESET_BUTTON_SIZE + 1,
                        0xFFFFFFFF);
            }

            // Check for mouse hover
            if (mouseX >= x && mouseX < x + PRESET_BUTTON_SIZE &&
                    mouseY >= y && mouseY < y + PRESET_BUTTON_SIZE) {
                graphics.renderTooltip(font, Component.translatable(presetName), mouseX, mouseY);
            }
        }
    }

    private void renderColorPreview(GuiGraphics graphics) {
        int centerX = width / 2;
        int previewSize = 40;
        int previewX = centerX + 120;
        int previewY = height / 2 - 60;

        graphics.drawString(font, Component.translatable("gui.colorvariants.preview"),
                previewX, previewY - 15, 0xFFFFFF);

        // Draw preview box
        ColorTransform currentTransform = new ColorTransform(hue, saturation, brightness);
        int previewColor = getPreviewColor(currentTransform);

        graphics.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, 0xFF000000);
        graphics.fill(previewX + 2, previewY + 2, previewX + previewSize - 2, previewY + previewSize - 2, previewColor);
    }

    private int getPreviewColor(ColorTransform transform) {
        // Use a test gray color to show the transformation
        int testColor = 0xFF808080; // Medium gray
        return transform.apply(testColor) | 0xFF000000; // Ensure full alpha
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check preset button clicks
        int startX = 10;
        int startY = 60;

        for (int i = 0; i < Math.min(presetNames.size(), 12); i++) {
            int col = i % PRESET_COLUMNS;
            int row = i / PRESET_COLUMNS;
            int x = startX + col * (PRESET_BUTTON_SIZE + 4);
            int y = startY + row * (PRESET_BUTTON_SIZE + 4);

            if (mouseX >= x && mouseX < x + PRESET_BUTTON_SIZE &&
                    mouseY >= y && mouseY < y + PRESET_BUTTON_SIZE) {
                applyPreset(i);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void applyPreset(int index) {
        selectedPresetIndex = index;
        String presetName = presetNames.get(index + presetScrollOffset);
        ColorTransform preset = ColorPresets.getPreset(presetName);

        hue = preset.getHueShift();
        saturation = preset.getSaturation();
        brightness = preset.getBrightness();

        hueSlider.setValue(hue);
        saturationSlider.setValue(saturation);
        brightnessSlider.setValue(brightness);

        updatePreview();
    }

    private void updatePreview() {
        // Could trigger a client-side preview here
    }

    private void applyColor() {
        ColorTransform transform = new ColorTransform(hue, saturation, brightness);
        PacketHandler.sendToServer(new ColorUpdatePacket(pos, transform));
        onClose();
    }

    private void resetColor() {
        hue = 0;
        saturation = 1;
        brightness = 1;
        hueSlider.setValue(hue);
        saturationSlider.setValue(saturation);
        brightnessSlider.setValue(brightness);
        updatePreview();
    }

    private void saveCustomPreset() {
        // Open a text input dialog to name the preset
        // For now, just use a default name
        ColorTransform transform = new ColorTransform(hue, saturation, brightness);
        String customName = "custom.preset_" + System.currentTimeMillis();
        ColorPresets.saveCustomPreset(customName, transform);
        loadPresets();
    }

    private void activateEyedropper() {
        // Implement eyedropper functionality
        // Would need to sample color from a block
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
