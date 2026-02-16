package com.colorvariants.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

/**
 * A slider widget for selecting values within a range.
 */
public class SliderWidget extends AbstractSliderButton {
    
    private final Component prefix;
    private final float min;
    private final float max;
    private final Consumer<Float> onValueChanged;
    
    public SliderWidget(
        int x, int y, int width, int height,
        Component prefix,
        float min, float max, float initialValue,
        Consumer<Float> onValueChanged
    ) {
        super(x, y, width, height, Component.empty(), 
            (initialValue - min) / (max - min));
        
        this.prefix = prefix;
        this.min = min;
        this.max = max;
        this.onValueChanged = onValueChanged;
        
        updateMessage();
    }
    
    @Override
    protected void updateMessage() {
        float value = getValue();
        setMessage(Component.empty()
            .append(prefix)
            .append(": ")
            .append(String.format("%.2f", value)));
    }
    
    @Override
    protected void applyValue() {
        if (onValueChanged != null) {
            onValueChanged.accept(getValue());
        }
    }
    
    /**
     * Gets the current value of the slider.
     */
    public float getValue() {
        return min + (float)(value * (max - min));
    }
    
    /**
     * Sets the value of the slider.
     */
    public void setValue(float newValue) {
        this.value = (newValue - min) / (max - min);
        this.value = Mth.clamp(this.value, 0.0, 1.0);
        updateMessage();
    }
}
