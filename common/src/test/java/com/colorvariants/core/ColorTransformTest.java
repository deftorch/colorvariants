package com.colorvariants.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorTransformTest {

    @Test
    public void testIsNone() {
        ColorTransform transform = ColorTransform.NONE;
        assertTrue(transform.isNone());

        ColorTransform other = new ColorTransform(10, 1.5f, 0.5f);
        assertFalse(other.isNone());
    }

    @Test
    public void testApply() {
        ColorTransform transform = new ColorTransform(0, 1, 1);
        int originalColor = 0xFFFFFFFF;
        int newColor = transform.apply(originalColor);
        assertEquals(originalColor, newColor);
    }
}
