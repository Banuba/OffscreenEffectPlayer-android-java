package com.banuba.offscreen.app.orientation;

import android.content.Context;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import java.util.Objects;

public abstract class FilteredOrientationEventListener extends OrientationEventListener {

    private static final int ORIENTATION_PORTRAIT_NORMAL = 0;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 90;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 180;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 270;
    private final Display mDisplay;

    private int mCurrentOrientationAngle = -90;
    private int mCurrentSurfaceRotation = -1;

    public FilteredOrientationEventListener(Context context) {
        super(context);
        mDisplay = Objects.requireNonNull((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    @Override
    final public void onOrientationChanged(int orientation) {
        final int surfaceRotation = mDisplay.getRotation();

        if (orientation < 35 || 325 <= orientation) {
            tryApply(ORIENTATION_PORTRAIT_NORMAL, surfaceRotation);
        } else if (235 <= orientation && orientation < 305) {
            tryApply(ORIENTATION_LANDSCAPE_NORMAL, surfaceRotation);
        } else if (145 <= orientation && orientation < 215) {
            tryApply(ORIENTATION_PORTRAIT_INVERTED, surfaceRotation);
        } else if (55 <= orientation && orientation < 125) {
            tryApply(ORIENTATION_LANDSCAPE_INVERTED, surfaceRotation);
        }


    }

    private void tryApply(int orientationAngle, int surfaceRotation) {
        if (mCurrentOrientationAngle != orientationAngle ||
            mCurrentSurfaceRotation !=surfaceRotation) {
            onOrientationFilteredChanged(orientationAngle, surfaceRotation);
            mCurrentOrientationAngle = orientationAngle;
            mCurrentSurfaceRotation = surfaceRotation;
        }
    }

    abstract public void onOrientationFilteredChanged(int sensorOrientation, int surfaceRotation);

}
