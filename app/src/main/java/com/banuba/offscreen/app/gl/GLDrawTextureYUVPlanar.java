// Developed by Banuba Development
// http://www.banuba.com
package com.banuba.offscreen.app.gl;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.banuba.sdk.internal.gl.GlUtils;


public class GLDrawTextureYUVPlanar extends GLDrawTextureYUVBase {


    private static final String SHADER_FRAG_START_3_TEXTURES = " " +
            "  precision highp float;                                                          \n" +
            "  varying vec2 v_texCoord;                                                        \n" +
            "  uniform sampler2D s_baseMapY;                                                   \n" +
            "  uniform sampler2D s_baseMapCh1;                                                 \n" +
            "  uniform sampler2D s_baseMapCh2;                                                 \n" +
            "  void main()                                                                     \n" +
            "  {                                                                               \n" +
            "     float y = texture2D(s_baseMapY, v_texCoord).r;                               \n" +
            "     float tu = texture2D(s_baseMapCh1, v_texCoord).r;                            \n" +
            "     float tv = texture2D(s_baseMapCh2, v_texCoord).r;                            \n";

    private final int mUniformSamplerY;
    private final int mUniformSamplerCh1;
    private final int mUniformSamplerCh2;

    public GLDrawTextureYUVPlanar(boolean swapColors) {
        super(swapColors);

        // Fragment Shader
        mUniformSamplerY = GLES20.glGetUniformLocation(mProgramHandle, "s_baseMapY");
        mUniformSamplerCh1 = GLES20.glGetUniformLocation(mProgramHandle, "s_baseMapCh1");
        mUniformSamplerCh2 = GLES20.glGetUniformLocation(mProgramHandle, "s_baseMapCh2");

    }

    @NonNull
    @Override
    protected String getShaderStart(boolean swapColors) {

        final String swapCode = swapColors ?
                " float u = tv - 0.5;\n float v = tu - 0.5;\n" :
                " float u = tu - 0.5;\n float v = tv - 0.5;\n";

        return SHADER_FRAG_START_3_TEXTURES + swapCode;
    }

    public void draw(boolean flipVertical, @NonNull final int[] textures, @NonNull float[] vertexMatrix, @NonNull float[] textureMatrix) {
        setupDraw(flipVertical, vertexMatrix, textureMatrix);

        // Fragment Shader - Texture
        GlUtils.setupSampler(0, mUniformSamplerY, textures[0], false);
        GlUtils.setupSampler(1, mUniformSamplerCh1, textures[1], false);
        GlUtils.setupSampler(2, mUniformSamplerCh2, textures[2], false);

        drawAndClear();
    }

}
