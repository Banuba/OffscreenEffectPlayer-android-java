package com.banuba.offscreen.app.camera;

public interface ICamera2Simple {

    void openCameraAndStartPreview();

    void stopPreviewAndCloseCamera();

    void switchFacing();

    void applyOrientationAngles(int sensorAngle, int surfaceRotation);

}
