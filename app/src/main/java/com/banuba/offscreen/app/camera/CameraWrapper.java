package com.banuba.offscreen.app.camera;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.banuba.offscreen.app.orientation.FilteredOrientationEventListener;
import com.banuba.sdk.internal.camera.CameraListener;
import com.banuba.sdk.offscreen.OffscreenEffectPlayer;
import com.banuba.sdk.types.FullImageData;

public class CameraWrapper implements DefaultLifecycleObserver {

    private static final String TAG = "BnBCamera";

    private final Camera2SimpleHandler mCameraHandler;
    private final FilteredOrientationEventListener mOrientationListener;

    public CameraWrapper(@NonNull Context context, @NonNull OffscreenEffectPlayer player, @NonNull Size preview, @NonNull Lifecycle lifecycle) {

        final CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpenError(Throwable throwable) {

            }

            @Override
            public void onCameraStatus(boolean b) {

            }


            public void onRecordingChanged(boolean started) {

            }

            @Override
            public void onHighResPhoto(@NonNull FullImageData fullImageData) {

            }
        };

        lifecycle.addObserver(this);

        mCameraHandler = new Camera2SimpleThread(context, player, cameraListener, preview).startAndGetHandler();

        mOrientationListener = new FilteredOrientationEventListener(context) {

            @MainThread
            @Override
            public void onOrientationFilteredChanged(int deviceSensorOrientation, int displaySurfaceRotation) {
                mCameraHandler.sendOrientationAngles(deviceSensorOrientation, displaySurfaceRotation);
            }
        };

    }


    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Log.e(TAG, "Camera Lifecycle OnStart ");
        mCameraHandler.sendOpenCamera();
        mOrientationListener.enable();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Log.e(TAG, "Camera Lifecycle OnStop ");
        mCameraHandler.sendCloseCamera();
        mOrientationListener.disable();
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        Log.e(TAG, "Camera Lifecycle onDestroy ");
        mCameraHandler.sendShutdown();
    }

    public void sendSwitchCamera() {
        mCameraHandler.sendSwitchCamera();
    }
}
