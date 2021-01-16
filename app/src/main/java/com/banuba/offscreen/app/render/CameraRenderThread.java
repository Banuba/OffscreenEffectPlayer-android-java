package com.banuba.offscreen.app.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LifecycleObserver;

import com.banuba.offscreen.app.BuffersQueue;
import com.banuba.offscreen.app.gl.GLDrawTextureYUVPlanar;
import com.banuba.offscreen.app.gl.GlViewPortSize;
import com.banuba.sdk.internal.BaseWorkThread;
import com.banuba.sdk.internal.gl.EglCore;
import com.banuba.sdk.internal.gl.GlUtils;
import com.banuba.sdk.internal.gl.OffscreenSurface;
import com.banuba.sdk.internal.gl.WindowSurface;
import com.banuba.sdk.offscreen.ImageProcessResult;
import com.banuba.sdk.offscreen.ImageProcessedListener;
import com.banuba.sdk.offscreen.OffscreenEffectPlayer;

import java.nio.ByteBuffer;
import java.util.Objects;

public class CameraRenderThread extends BaseWorkThread<CameraRenderHandler> implements LifecycleObserver {


    private final SurfaceHolder mSurfaceHolder;
    private final OffscreenEffectPlayer mOffscreenEffectPlayer;

    private final float[] mIdentity = new float[16];
    private final int[] mTextures = new int[3];
    private final Size mSize;
    private final BuffersQueue mBuffersQueue;

    private CameraRenderHandler mHandler;
    private WindowSurface mWindowSurface;
    private EglCore mEglCore;

    private GlViewPortSize mSurfaceFullViewPort;
    private GlViewPortSize mScreenViewPort169;

    private GLDrawTextureYUVPlanar mGLDrawTextureYUVPlanar;
    private final Display mDefaultDisplay;

    public CameraRenderThread(@NonNull Context context, @NonNull Size size, @NonNull OffscreenEffectPlayer offscreenEffectPlayer, @NonNull SurfaceHolder holder, @Nullable BuffersQueue buffersQueue) {
        super("CameraRenderThread");
        mSize = size;
        mSurfaceHolder = holder;
        mBuffersQueue = buffersQueue;

        mDefaultDisplay = Objects.requireNonNull((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        mOffscreenEffectPlayer = offscreenEffectPlayer;
        Matrix.setIdentityM(mIdentity, 0);

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {

            // Always Called on UI Thread

            @MainThread
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d("ANDREY5", "[" + Thread.currentThread().getId() + "] handleSurfaceCreated !!! ");
                mHandler.sendSurfaceCreated();
            }

            @MainThread
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d("ANDREY5", "[" + Thread.currentThread().getId() + "] handleSurfaceChanged !!! ");
                mHandler.sendSurfaceChanged(width, height);
            }

            @MainThread
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d("ANDREY5", "[" + Thread.currentThread().getId() + "] handleSurfaceDestroyed !!! ");
                mHandler.sendSurfaceDestroyed();
            }
        });
    }

    private final ImageProcessedListener mListener = new ImageProcessedListener() {
        @Override
        public void onImageProcessed(@NonNull ImageProcessResult result) {
            mHandler.sendDrawFrame(result);
        }
    };


    private void loadProcessResult2Textures(@NonNull ImageProcessResult result) {

        final ByteBuffer buffer = result.getBuffer();

        for (int i = 0; i < 3; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE,
                    result.getRowStride(i), result.getPlaneHeight(i),
                    0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE,
                    buffer.position(result.getPlaneOffset(i)));
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }


    @NonNull
    @Override
    protected CameraRenderHandler constructHandler() {
        mHandler = new CameraRenderHandler(this);
        mOffscreenEffectPlayer.setImageProcessListener(mListener, mHandler);
        return mHandler;
    }

    @WorkerThread
    protected void preRunInit() {

        Log.d("ANDREY5", "[" + Thread.currentThread().getId() + "] PRE RUN INIT !!! ");

        mEglCore = new EglCore(null, 0x2);

        final OffscreenSurface surface = new OffscreenSurface(mEglCore, 16, 16);
        surface.makeCurrent();

        makeTextures(mTextures);

        mGLDrawTextureYUVPlanar = new GLDrawTextureYUVPlanar(false);

    }

    @WorkerThread
    protected void postRunClear() {

        final WindowSurface windowSurface = mWindowSurface;
        if (windowSurface != null) {
            windowSurface.release();
        }

        mEglCore.release();
    }

    @WorkerThread
    void handleSurfaceCreated() {
        Log.d("ANDREY5", "[" + Thread.currentThread().getId() + "] handleSurfaceCreated !!! ");
        final Surface surface = mSurfaceHolder.getSurface();

        mWindowSurface = new WindowSurface(mEglCore, surface, false);
        mWindowSurface.makeCurrent();

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GlUtils.checkGlErrorNoException("prepareGl");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    }


    @WorkerThread
    void handleSurfaceChanged(int width, int height) {
        mScreenViewPort169 = GlViewPortSize.makeViewPort(width, height, mSize);
        mSurfaceFullViewPort = GlViewPortSize.fullViewPort(width, height);
    }

    public void handleDrawFrame(@NonNull ImageProcessResult imageProcessResult) {

        final int displaySurfaceRotation = mDefaultDisplay.getRotation();
        if (imageProcessResult.getOrientation().getRotationIndex() == displaySurfaceRotation) {

            loadProcessResult2Textures(imageProcessResult);

            final float[] matrix = imageProcessResult.getOrientation().getDefaultDrawMatrix(displaySurfaceRotation);

            mSurfaceFullViewPort.apply();
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            mScreenViewPort169.apply();
            mGLDrawTextureYUVPlanar.draw(false, mTextures, mIdentity, matrix);
            mWindowSurface.swapBuffers();

        }

        if (mBuffersQueue != null) {
            mBuffersQueue.retainBuffer(imageProcessResult.getBuffer());
        }

    }

    public void handleSurfaceDestroyed() {

    }


    public static void makeTextures(int[] textures) {

        GLES20.glGenTextures(textures.length, textures, 0);

        for (int texture : textures) {
            if (texture == 0) {
                throw new RuntimeException("Error loading texture.");
            }
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }


}

