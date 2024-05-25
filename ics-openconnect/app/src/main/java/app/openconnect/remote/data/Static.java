package app.openconnect.remote.data;

import android.app.Activity;

import androidx.annotation.NonNull;

public class Static {
    // All data
    private static Global globalData;

    public static Global getGlobalData() {
        return globalData;
    }

    public static void setGlobalData(@NonNull Activity context) {
        globalData = new Global(context);
    }
}
