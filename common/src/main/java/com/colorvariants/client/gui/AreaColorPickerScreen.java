package com.colorvariants.client.gui;

import com.colorvariants.core.ColorTransform;
import com.colorvariants.network.AreaColorUpdatePacket;
import com.colorvariants.network.PacketHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for coloring multiple blocks in a selected area.
 */
public class AreaColorPickerScreen extends Screen {

    private final BlockPos pos1;
    private final BlockPos pos2;

    private SliderWidget hueSlider;
    private SliderWidget saturationSlider;
    private SliderWidget brightnessSlider;

    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;

    private Checkbox replaceSameTypeOnly;
    private Checkbox previewMode;

    private Button applyButton;
    private Button cancelButton;

    private int blockCount;

    public AreaColorPickerScreen(BlockPos pos1, BlockPos pos2) {
        super(Component.translatable("gui.colorvariants.area_color_picker.title"));
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.blockCount = calculateBlockCount();
    }

    private int calculateBlockCount() {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;
        int centerY = height / 2;

        // Info text
        int infoY = centerY - 100;

        // Sliders
        int sliderWidth = 200;
        int sliderX = centerX - sliderWidth / 2;
        int startY = centerY - 50;

        hueSlider = new SliderWidget(
                sliderX, startY, sliderWidth, 20,
                Component.translatable("gui.colorvariants.hue"),
                0, 360, hue,
                value -> {
                    hue = value;
                    if (previewMode != null && previewMode.selected()) {
                        updatePreview();
                    }
                });
        addRenderableWidget(hueSlider);

        saturationSlider = new SliderWidget(
                sliderX, startY + 25, sliderWidth, 20,
                Component.translatable("gui.colorvariants.saturation"),
                0, 2, saturation,
                value -> {
                    saturation = value;
                    if (previewMode != null && previewMode.selected()) {
                        updatePreview();
                    }
                });
        addRenderableWidget(saturationSlider);

        brightnessSlider = new SliderWidget(
                sliderX, startY + 50, sliderWidth, 20,
                Component.translatable("gui.colorvariants.brightness"),
                0, 2, brightness,
                value -> {
                    brightness = value;
                    if (previewMode != null && previewMode.selected()) {
                        updatePreview();
                    }
                });
        addRenderableWidget(brightnessSlider);

        // Checkboxes
        int checkboxY = startY + 80;

        replaceSameTypeOnly = new Checkbox(
                sliderX, checkboxY, 150, 20,
                Component.translatable("gui.colorvariants.same_type_only"),
                false);
        addRenderableWidget(replaceSameTypeOnly);

        previewMode = new Checkbox(
                sliderX, checkboxY + 25, 150, 20,
                Component.translatable("gui.colorvariants.preview_mode"),
                false);
        addRenderableWidget(previewMode);

        // Buttons
        int buttonY = checkboxY + 60;
        int buttonWidth = 80;

        applyButton = Button.builder(
                Component.translatable("gui.colorvariants.apply_all"),
                btn -> applyToArea()).bounds(centerX - buttonWidth - 5, buttonY, buttonWidth, 20).build();
        addRenderableWidget(applyButton);

        cancelButton = Button.builder(
                Component.translatable("gui.colorvariants.cancel"),
                btn -> onClose()).bounds(centerX + 5, buttonY, buttonWidth, 20).build();
        addRenderableWidget(cancelButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(graphics);

        // Title
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        // Area info
        int centerX = width / 2;
        int infoY = height / 2 - 100;

        graphics.drawCenteredString(font,
                Component.translatable("gui.colorvariants.area_selection",
                        pos1.getX(), pos1.getY(), pos1.getZ(),
                        pos2.getX(), pos2.getY(), pos2.getZ()),
                centerX, infoY, 0xAAAAAA);

        graphics.drawCenteredString(font,
                Component.translatable("gui.colorvariants.block_count", blockCount),
                centerX, infoY + 15, 0xFFFF55);

        // Warning for large selections
        if (blockCount > 1000) {
            graphics.drawCenteredString(font,
                    Component.translatable("gui.colorvariants.large_selection_warning"),
                    centerX, infoY + 30, 0xFF5555);
        }

        // Current values
        graphics.drawCenteredString(font,
                String.format("H: %.0f°  S: %.2f  B: %.2f", hue, saturation, brightness),
                centerX, height / 2 + 40, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void updatePreview() {
        // Send preview packet to server (temporary, non-saved)
        // This would need a separate packet type
    }

    private void applyToArea() {
        ColorTransform transform = new ColorTransform(hue, saturation, brightness);

        // Get all positions in the selected area
        List<BlockPos> positions = getBlocksInArea();

        // Send packet to server
        PacketHandler.sendToServer(new AreaColorUpdatePacket(
                positions,
                transform,
                replaceSameTypeOnly.selected()));

        onClose();
    }

    private List<BlockPos> getBlocksInArea() {
        List<BlockPos> positions = new ArrayList<>();

        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }

        return positions;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
