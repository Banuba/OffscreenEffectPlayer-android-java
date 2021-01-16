package com.banuba.offscreen.app;

import android.content.Context;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public final class Utils {

    private Utils() {

    }



    public static int getSurfaceRotation(@NonNull Context context) {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            return windowManager.getDefaultDisplay().getRotation();
        }
        return Surface.ROTATION_0;
    }


}
