package com.colorvariants.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.colorvariants.core.ColorTransform;
import com.colorvariants.network.ColorUpdatePacket;
import com.colorvariants.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * GUI screen for selecting colors using HSV sliders.
 */
public class ColorPickerScreen extends Screen {

    private final BlockPos pos;
    private final BlockState state;

    private SliderWidget hueSlider;
    private SliderWidget saturationSlider;
    private SliderWidget brightnessSlider;

    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;

    private Button applyButton;
    private Button resetButton;
    private Button cancelButton;

    public ColorPickerScreen(BlockPos pos, BlockState state) {
        super(Component.translatable("gui.colorvariants.color_picker.title"));
        this.pos = pos;
        this.state = state;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Title
        int titleY = centerY - 80;

        // Sliders
        int sliderWidth = 200;
        int sliderX = centerX - sliderWidth / 2;

        hueSlider = new SliderWidget(
                sliderX, centerY - 30, sliderWidth, 20,
                Component.translatable("gui.colorvariants.hue"),
                0, 360, hue,
                value -> {
                    hue = value;
                    updatePreview();
                });
        addRenderableWidget(hueSlider);

        saturationSlider = new SliderWidget(
                sliderX, centerY, sliderWidth, 20,
                Component.translatable("gui.colorvariants.saturation"),
                0, 2, saturation,
                value -> {
                    saturation = value;
                    updatePreview();
                });
        addRenderableWidget(saturationSlider);

        brightnessSlider = new SliderWidget(
                sliderX, centerY + 30, sliderWidth, 20,
                Component.translatable("gui.colorvariants.brightness"),
                0, 2, brightness,
                value -> {
                    brightness = value;
                    updatePreview();
                });
        addRenderableWidget(brightnessSlider);

        // Buttons
        int buttonWidth = 60;
        int buttonY = centerY + 70;

        applyButton = Button.builder(
                Component.translatable("gui.colorvariants.apply"),
                btn -> applyColor()).bounds(centerX - buttonWidth - 5, buttonY, buttonWidth, 20).build();
        addRenderableWidget(applyButton);

        resetButton = Button.builder(
                Component.translatable("gui.colorvariants.reset"),
                btn -> resetColor()).bounds(centerX - buttonWidth / 2, buttonY + 25, buttonWidth, 20).build();
        addRenderableWidget(resetButton);

        cancelButton = Button.builder(
                Component.translatable("gui.colorvariants.cancel"),
                btn -> onClose()).bounds(centerX + 5, buttonY, buttonWidth, 20).build();
        addRenderableWidget(cancelButton);
    }

    private void updatePreview() {
        // Preview logic could be added here
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
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw title
        graphics.drawCenteredString(
                font,
                title,
                width / 2,
                height / 2 - 90,
                0xFFFFFF);

        // Draw block name
        Component blockName = state.getBlock().getName();
        graphics.drawCenteredString(
                font,
                blockName,
                width / 2,
                height / 2 - 75,
                0xAAAAAA);

        // Draw color preview
        drawColorPreview(graphics);
    }

    private void drawColorPreview(GuiGraphics graphics) {
        int x = width / 2 - 25;
        int y = height / 2 + 100;
        int size = 50;

        // Create a sample color
        ColorTransform transform = new ColorTransform(hue, saturation, brightness);
        int baseColor = 0xFFFF0000; // Red base
        int transformedColor = transform.apply(baseColor);

        // Extract RGB
        int r = (transformedColor >> 16) & 0xFF;
        int g = (transformedColor >> 8) & 0xFF;
        int b = transformedColor & 0xFF;

        int color = 0xFF000000 | (r << 16) | (g << 8) | b;

        // Draw preview box
        graphics.fill(x, y, x + size, y + size, color);
        graphics.fill(x, y, x + size, y + 1, 0xFFFFFFFF); // Top border
        graphics.fill(x, y + size - 1, x + size, y + size, 0xFFFFFFFF); // Bottom
        graphics.fill(x, y, x + 1, y + size, 0xFFFFFFFF); // Left
        graphics.fill(x + size - 1, y, x + size, y + size, 0xFFFFFFFF); // Right
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
