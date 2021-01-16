package com.banuba.offscreen.app.gl;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.banuba.sdk.internal.gl.GlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public abstract class GLDrawTextureYUVBase {

    public static final int FLOAT_SIZE = 4;

    public static final int COORDS_PER_VERTEX = 3;
    public static final int COORDS_UV_PER_TEXTURE = 2;

    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * FLOAT_SIZE;
    public static final int TEXTURE_STRIDE = COORDS_UV_PER_TEXTURE * FLOAT_SIZE;


    // Conversion YUV to RGB Based on Android sources (float variant)
    // https://android.googlesource.com/platform/frameworks/av/+/master/media/libstagefright/colorconversion/ColorConverter.cpp
    // B = 1.164 * (Y - 16) + 2.018 * (U - 128)
    // G = 1.164 * (Y - 16) - 0.813 * (V - 128) - 0.391 * (U - 128)
    // R = 1.164 * (Y - 16) + 1.596 * (V - 128)

    private static final String SHADER_FRAG_CONVERSION_CODE = " \n" +
            "     y = y - 0.0625;                               \n" +
            "     float b = 1.164 * y + 2.018 * u;              \n" +
            "     float g = 1.164 * y - 0.813 * v - 0.391 * u;  \n" +
            "     float r = 1.164 * y + 1.596 * v;              \n" +
            "     gl_FragColor = vec4(r, g, b, 1.0);            \n" +
            "  }  \n";


    private static final String SHADER_VERTEX = " " +
            "  uniform mat4 uTextureMatrix;                     \n" +
            "  uniform mat4 uVertexMatrix;                      \n" +
            "  attribute vec4 a_position;                       \n" +
            "  attribute vec2 a_texCoord;                       \n" +
            "  varying vec2 v_texCoord;                         \n" +
            "  void main()                                      \n" +
            "  {                                                \n" +
            "     gl_Position = uVertexMatrix * a_position;     \n" +
            "     vec4 texCoord = vec4(a_texCoord, 0.0, 1.0);   \n" +
            "     v_texCoord = (uTextureMatrix * texCoord).xy;  \n" +
            "  }                                                \n";

    protected final int mProgramHandle;

    private final int mVertexCount = RECTANGLE_VERTEX.length / COORDS_PER_VERTEX;
    private final int mAttributePosition;
    private final int mAttributeTextureCoord;
    private final int mUniformVertexMatrix;
    private final int mUniformTextureMatrix;
    private final int[] mVBO;

    private static final float[] RECTANGLE_TEXTURE_UV = {
            0.0f, 0.0f,     // 0 bottom left
            1.0f, 0.0f,     // 1 bottom right
            0.0f, 1.0f,     // 2 top left
            1.0f, 1.0f      // 3 top right
    };

    private static final float[] RECTANGLE_TEXTURE_UV_SWAP = {
            0.0f, 1.0f,     // 0 bottom left
            1.0f, 1.0f,     // 1 bottom right
            0.0f, 0.0f,     // 2 top left
            1.0f, 0.0f      // 3 top right
    };

    private static final float[] RECTANGLE_VERTEX = new float[]{
            -1f, -1f, 0.0f,          // 0 bottom left
            1f, -1f, 0.0f,           // 1 bottom right
            -1f, 1f, 0.0f,           // 2 top left
            1f, 1f, 0.0f,            // 3 top right
    };


    GLDrawTextureYUVBase(boolean swapColors) {

        mVBO = new int[3];
        GLES20.glGenBuffers(mVBO.length, mVBO, 0);

        loadBufferData(mVBO[0], RECTANGLE_VERTEX);
        loadBufferData(mVBO[1], RECTANGLE_TEXTURE_UV);
        loadBufferData(mVBO[2], RECTANGLE_TEXTURE_UV_SWAP);

        mProgramHandle = GlUtils.loadProgram(SHADER_VERTEX, getShaderStart(swapColors) + SHADER_FRAG_CONVERSION_CODE);

        // Vertex shader
        mAttributePosition = GLES20.glGetAttribLocation(mProgramHandle, "a_position");
        mAttributeTextureCoord = GLES20.glGetAttribLocation(mProgramHandle, "a_texCoord");

        mUniformVertexMatrix = GLES20.glGetUniformLocation(mProgramHandle, "uVertexMatrix");
        mUniformTextureMatrix = GLES20.glGetUniformLocation(mProgramHandle, "uTextureMatrix");

    }

    public static void loadBufferData(int bufferId, @NonNull float[] array) {
        final FloatBuffer floatBuffer = createFloatBuffer(array);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId);
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                array.length * FLOAT_SIZE,
                floatBuffer,
                GLES20.GL_STATIC_DRAW);
    }

    private static FloatBuffer createFloatBuffer(@NonNull float[] coords) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        final FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.rewind();
        return fb;
    }

    @NonNull
    protected abstract String getShaderStart(boolean swapColors);

    void setupDraw(boolean swap, @NonNull float[] vertexMatrix, @NonNull float[] textureMatrix) {

        GLES20.glUseProgram(mProgramHandle);

        // Vertex Shader Buffers
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO[0]);
        GLES20.glVertexAttribPointer(mAttributePosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, 0);
        GLES20.glEnableVertexAttribArray(mAttributePosition);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, swap ? mVBO[2] : mVBO[1]);
        GLES20.glVertexAttribPointer(mAttributeTextureCoord, COORDS_UV_PER_TEXTURE, GLES20.GL_FLOAT, false, TEXTURE_STRIDE, 0);
        GLES20.glEnableVertexAttribArray(mAttributeTextureCoord);

        // Vertex Shader - Uniforms
        GLES20.glUniformMatrix4fv(mUniformVertexMatrix, 1, false, vertexMatrix, 0);
        GLES20.glUniformMatrix4fv(mUniformTextureMatrix, 1, false, textureMatrix, 0);

    }

    void drawAndClear() {

        // Drawing
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);

        GLES20.glDisableVertexAttribArray(mAttributePosition);
        GLES20.glDisableVertexAttribArray(mAttributeTextureCoord);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glUseProgram(0);

    }

    public void release() {
        GLES20.glDeleteProgram(mProgramHandle);
        GLES20.glDeleteBuffers(mVBO.length, mVBO, 0);
    }
}