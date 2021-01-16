package com.banuba.offscreen.app.render;

import android.os.Message;

import androidx.annotation.NonNull;

import com.banuba.sdk.internal.WeakHandler;
import com.banuba.sdk.offscreen.ImageProcessResult;

public class CameraRenderHandler extends WeakHandler<CameraRenderThread> {

    private static final int MSG_SHUTDOWN = 0;
    private static final int MSG_SURFACE_CREATED = 1;
    private static final int MSG_SURFACE_CHANGED = 2;
    private static final int MSG_SURFACE_DESTROYED = 3;
    private static final int MSG_DRAW_FRAME = 4;

    public CameraRenderHandler(@NonNull CameraRenderThread cameraRenderThread) {
        super(cameraRenderThread);
    }


    public void sendSurfaceCreated() {
        sendMessage(obtainMessage(MSG_SURFACE_CREATED));
    }

    public void sendSurfaceChanged(int width, int height) {
        sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height));
    }

    public void sendSurfaceDestroyed() {
        sendMessage(obtainMessage(MSG_SURFACE_DESTROYED));
    }

    public void sendDrawFrame(@NonNull ImageProcessResult imageProcessResult) {
        sendMessage(obtainMessage(MSG_DRAW_FRAME, imageProcessResult));
    }

    public void sendShutdown() {
        sendMessage(obtainMessage(MSG_SHUTDOWN));
    }

    @Override  // runs on RenderThread
    public void handleMessage(@NonNull Message msg) {
        final CameraRenderThread thread = getThread();
        if (thread != null && msg.getCallback() == null) {
            switch (msg.what) {
                case MSG_SHUTDOWN:
                    thread.shutdown();
                    break;
                case MSG_SURFACE_CREATED:
                    thread.handleSurfaceCreated();
                    break;
                case MSG_SURFACE_CHANGED:
                    thread.handleSurfaceChanged(msg.arg1, msg.arg2);
                    break;
                case MSG_SURFACE_DESTROYED:
                    thread.handleSurfaceDestroyed();
                    break;
                case MSG_DRAW_FRAME:
                    final ImageProcessResult imageProcessResult = (ImageProcessResult) msg.obj;
                    thread.handleDrawFrame(imageProcessResult);
                    break;
                default:
                    throw new RuntimeException("Unknown message " + msg.what);
            }
        }
    }

}
