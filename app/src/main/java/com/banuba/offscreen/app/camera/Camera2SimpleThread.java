// Developed by Banuba Development
// http://www.banuba.com
package com.banuba.offscreen.app.camera;

import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.util.Size;

import androidx.annotation.NonNull;

import com.banuba.sdk.internal.BaseWorkThread;
import com.banuba.sdk.internal.camera.CameraListener;
import com.banuba.sdk.internal.camera.CameraListenerHandler;
import com.banuba.sdk.offscreen.OffscreenEffectPlayer;

@SuppressWarnings("WeakerAccess")
public class Camera2SimpleThread extends BaseWorkThread<Camera2SimpleHandler> {

    private final Context mContext;
    private final OffscreenEffectPlayer mEffectPlayer;
    private final CameraListener mCameraListener;

    @NonNull
    private final Size mPreferredPreviewSize;

    private ICamera2Simple mCameraAPI;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public Camera2SimpleThread(
            @NonNull Context context,
            @NonNull OffscreenEffectPlayer effectPlayer,
            @NonNull CameraListener cameraListener,
            @NonNull Size preferredPreviewSize) {
        super("CameraThread");
        mContext = context;
        mEffectPlayer = effectPlayer;
        mCameraListener = cameraListener;
        mPreferredPreviewSize = preferredPreviewSize;
    }

    @NonNull
    @Override
    protected Camera2SimpleHandler constructHandler() {
        return new Camera2SimpleHandler(this);
    }

    @Override
    protected void preRunInit() {
        mCameraAPI = new Camera2Simple(
                mContext,
                mEffectPlayer,
                new CameraListenerHandler(mCameraListener),
                (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE),
                mPreferredPreviewSize);
    }

    public void handleOpenCamera() {
        mCameraAPI.openCameraAndStartPreview();
    }

    public void handleReleaseCamera() {
        mCameraAPI.stopPreviewAndCloseCamera();
    }

    public void handleSwitchCamera() {
        mCameraAPI.switchFacing();
    }

    public void handleOrientationAngles(int deviceSensorOrientationAngle, int displaySurfaceRotation) {
        mCameraAPI.applyOrientationAngles(deviceSensorOrientationAngle, displaySurfaceRotation);
    }

}
