// Developed by Banuba Development
// http://www.banuba.com
package com.banuba.offscreen.app.camera;

import android.os.Message;

import androidx.annotation.NonNull;

import com.banuba.sdk.internal.WeakHandler;
import com.banuba.sdk.internal.utils.Logger;

@SuppressWarnings("WeakerAccess")
public class Camera2SimpleHandler extends WeakHandler<Camera2SimpleThread> {

    private static final int MSG_SHUTDOWN = 0;
    private static final int MSG_OPEN_CAMERA = 1;
    private static final int MSG_CLOSE_CAMERA = 2;
    private static final int MSG_SWITCH_CAMERA = 3;
    private static final int MSG_ORIENTATION_ANGLES = 4;


    public Camera2SimpleHandler(Camera2SimpleThread cameraThread) {
        super(cameraThread);
    }

    public void sendCloseCamera() {
        sendMessage(obtainMessage(MSG_CLOSE_CAMERA));
    }

    public void sendOpenCamera() {
        sendMessage(obtainMessage(MSG_OPEN_CAMERA));
    }

    public void sendSwitchCamera() {
        sendMessage(obtainMessage(MSG_SWITCH_CAMERA));
    }

    public void sendOrientationAngles(int deviceSensorOrientation, int surfaceRotation) {
        sendMessage(obtainMessage(MSG_ORIENTATION_ANGLES, deviceSensorOrientation, surfaceRotation));
    }

    public void sendShutdown() {
        removeCallbacksAndMessages(null);
        sendMessage(obtainMessage(MSG_CLOSE_CAMERA));
        sendMessage(obtainMessage(MSG_SHUTDOWN));
    }

    public void handleMessage(@NonNull Message msg) {
        final Camera2SimpleThread thread = getThread();
        if (thread != null) {
            switch (msg.what) {
                case MSG_SHUTDOWN:
                    thread.shutdown();
                    break;
                case MSG_OPEN_CAMERA:
                    thread.handleOpenCamera();
                    break;
                case MSG_CLOSE_CAMERA:
                    thread.handleReleaseCamera();
                    break;
                case MSG_SWITCH_CAMERA:
                    thread.handleSwitchCamera();
                    break;
                case MSG_ORIENTATION_ANGLES:
                    thread.handleOrientationAngles(msg.arg1, msg.arg2);
                    break;
                default:
                    throw new RuntimeException("unknown message " + msg.what);
            }
        } else {
            Logger.w("Empty camera thread");
        }
    }
}
