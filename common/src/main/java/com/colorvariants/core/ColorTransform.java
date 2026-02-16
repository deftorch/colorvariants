package com.colorvariants.core;

import net.minecraft.nbt.CompoundTag;

/**
 * Represents a color transformation using HSV (Hue, Saturation, Value) values.
 * This class is immutable and thread-safe.
 */
public class ColorTransform {
    
    public static final ColorTransform NONE = new ColorTransform(0, 1, 1);
    
    private final float hueShift;       // 0-360 degrees
    private final float saturation;     // 0-2 multiplier
    private final float brightness;     // 0-2 multiplier
    
    /**
     * Creates a new color transform.
     * 
     * @param hueShift Hue shift in degrees (0-360)
     * @param saturation Saturation multiplier (0-2)
     * @param brightness Brightness multiplier (0-2)
     */
    public ColorTransform(float hueShift, float saturation, float brightness) {
        this.hueShift = clamp(hueShift, 0, 360);
        this.saturation = clamp(saturation, 0, 2);
        this.brightness = clamp(brightness, 0, 2);
    }
    
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public float getHueShift() {
        return hueShift;
    }
    
    public float getSaturation() {
        return saturation;
    }
    
    public float getBrightness() {
        return brightness;
    }
    
    /**
     * Applies this color transform to an RGB color.
     * 
     * @param rgb Original RGB color as packed integer
     * @return Transformed RGB color as packed integer
     */
    public int apply(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int a = (rgb >> 24) & 0xFF;
        
        // Convert RGB to HSV
        float[] hsv = rgbToHsv(r, g, b);
        
        // Apply transformations
        hsv[0] = (hsv[0] + hueShift) % 360;
        hsv[1] = clamp(hsv[1] * saturation, 0, 1);
        hsv[2] = clamp(hsv[2] * brightness, 0, 1);
        
        // Convert back to RGB
        int[] newRgb = hsvToRgb(hsv[0], hsv[1], hsv[2]);
        
        // Pack into integer
        return (a << 24) | (newRgb[0] << 16) | (newRgb[1] << 8) | newRgb[2];
    }
    
    /**
     * Converts RGB to HSV.
     * 
     * @param r Red (0-255)
     * @param g Green (0-255)
     * @param b Blue (0-255)
     * @return HSV array [hue (0-360), saturation (0-1), value (0-1)]
     */
    private float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        
        float hue = 0;
        float saturation = (max == 0) ? 0 : delta / max;
        float value = max;
        
        if (delta != 0) {
            if (max == rf) {
                hue = 60 * (((gf - bf) / delta) % 6);
            } else if (max == gf) {
                hue = 60 * (((bf - rf) / delta) + 2);
            } else {
                hue = 60 * (((rf - gf) / delta) + 4);
            }
        }
        
        if (hue < 0) hue += 360;
        
        return new float[] { hue, saturation, value };
    }
    
    /**
     * Converts HSV to RGB.
     * 
     * @param h Hue (0-360)
     * @param s Saturation (0-1)
     * @param v Value (0-1)
     * @return RGB array [r, g, b] (0-255)
     */
    private int[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs(((h / 60) % 2) - 1));
        float m = v - c;
        
        float r = 0, g = 0, b = 0;
        
        if (h >= 0 && h < 60) {
            r = c; g = x; b = 0;
        } else if (h >= 60 && h < 120) {
            r = x; g = c; b = 0;
        } else if (h >= 120 && h < 180) {
            r = 0; g = c; b = x;
        } else if (h >= 180 && h < 240) {
            r = 0; g = x; b = c;
        } else if (h >= 240 && h < 300) {
            r = x; g = 0; b = c;
        } else {
            r = c; g = 0; b = x;
        }
        
        return new int[] {
            Math.round((r + m) * 255),
            Math.round((g + m) * 255),
            Math.round((b + m) * 255)
        };
    }
    
    /**
     * Checks if this transform is effectively a no-op.
     */
    public boolean isNone() {
        return Math.abs(hueShift) < 0.01f && 
               Math.abs(saturation - 1) < 0.01f && 
               Math.abs(brightness - 1) < 0.01f;
    }
    
    /**
     * Saves this transform to NBT.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("HueShift", hueShift);
        tag.putFloat("Saturation", saturation);
        tag.putFloat("Brightness", brightness);
        return tag;
    }
    
    /**
     * Loads a transform from NBT.
     */
    public static ColorTransform load(CompoundTag tag) {
        if (tag == null) return NONE;
        
        float hue = tag.getFloat("HueShift");
        float sat = tag.getFloat("Saturation");
        float bright = tag.getFloat("Brightness");
        
        return new ColorTransform(hue, sat, bright);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ColorTransform)) return false;
        
        ColorTransform other = (ColorTransform) obj;
        return Math.abs(hueShift - other.hueShift) < 0.01f &&
               Math.abs(saturation - other.saturation) < 0.01f &&
               Math.abs(brightness - other.brightness) < 0.01f;
    }
    
    @Override
    public int hashCode() {
        return Float.hashCode(hueShift) * 31 + 
               Float.hashCode(saturation) * 17 + 
               Float.hashCode(brightness);
    }
    
    @Override
    public String toString() {
        return String.format("ColorTransform[H=%.1f, S=%.2f, B=%.2f]", 
            hueShift, saturation, brightness);
    }
}
