// Developed by Banuba Development
// http://www.banuba.com
package com.banuba.offscreen.app.gl;

import android.opengl.GLES20;
import android.util.Size;

import androidx.annotation.NonNull;

public class GlViewPortSize {

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public GlViewPortSize(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void apply() {
        GLES20.glViewport(x, y, width, height);
    }

    @NonNull
    public static GlViewPortSize fullViewPort(int surfaceWidth, int surfaceHeight) {
        return new GlViewPortSize(0, 0, surfaceWidth, surfaceHeight);
    }

    @NonNull
    public static GlViewPortSize makeViewPort(int surfaceWidth, int surfaceHeight, @NonNull Size size) {

        if (surfaceWidth < surfaceHeight) {
            // Vertical
            final int renderMin = Math.min(size.getWidth(), size.getHeight());
            final int renderMax = Math.max(size.getWidth(), size.getHeight());

            final float ratioW = surfaceWidth / (float) renderMin;
            final float ratioH = surfaceHeight / (float) renderMax;

            final float rationMin = Math.min(ratioW, ratioH);

            final int sideW = (int) (rationMin * renderMin);
            final int sideH = (int) (rationMin * renderMax);

            return new GlViewPortSize((surfaceWidth - sideW) / 2, (surfaceHeight - sideH) / 2, sideW, sideH);

        } else {
            // Horizontal

            final int renderMin = Math.min(size.getWidth(), size.getHeight());
            final int renderMax = Math.max(size.getWidth(), size.getHeight());

            final float ratioW = surfaceWidth / (float) renderMax;
            final float ratioH = surfaceHeight / (float) renderMin;

            final float rationMin = Math.min(ratioW, ratioH);

            final int sideW = (int) (rationMin * renderMax);
            final int sideH = (int) (rationMin * renderMin);

            return new GlViewPortSize((surfaceWidth - sideW) / 2, (surfaceHeight - sideH) / 2, sideW, sideH);
        }

    }

    @Override
    @NonNull
    public String toString() {
        return "GlViewPortSize{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
