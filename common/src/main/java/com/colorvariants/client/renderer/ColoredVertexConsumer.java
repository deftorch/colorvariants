package com.colorvariants.client.renderer;

import com.colorvariants.core.ColorTransform;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class ColoredVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final ColorTransform transform;

    public ColoredVertexConsumer(VertexConsumer delegate, ColorTransform transform) {
        this.delegate = delegate;
        this.transform = transform;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        int color = transform.apply((a << 24) | (r << 16) | (g << 8) | b);
        delegate.color(
            (color >> 16) & 0xFF,
            (color >> 8) & 0xFF,
            color & 0xFF,
            (color >> 24) & 0xFF
        );
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        int color = transform.apply((a << 24) | (r << 16) | (g << 8) | b);
        delegate.defaultColor(
            (color >> 16) & 0xFF,
            (color >> 8) & 0xFF,
            color & 0xFF,
            (color >> 24) & 0xFF
        );
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}
