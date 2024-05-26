package de.blinkt.openvpn.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * MehrabSp
 * This is init file for OpenVpn client
 */
public abstract class App extends sp.xray.lite.AngApplication { // extends /*com.orm.SugarApp*/ Application
    public static String ContentTitle = "OpenVpn"; // Notif title
    static NotificationManager manager;
    static ArrayList<String> appsList = new ArrayList<>(); // SplitTunnel Apps
    public static Boolean isShowToastOpenVpn = false;

    protected abstract String getContentTitle();

    @NotNull
    protected abstract String getChannelID(); // com.example.myapp

    @NotNull
    protected abstract String getChannelIDName(); // comexamplemyapp

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            createNotificationChannel(this, getChannelID(), getChannelIDName());
            String notifTitle = getContentTitle();
            if(notifTitle != null) {
                ContentTitle = getContentTitle();
            }

            PRNGFixes.apply();
            StatusListener mStatus = new StatusListener();
            mStatus.init(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * by MehrabSp
     * for split tunnel
     * @param packageName Example: com.android.chrome
     *  or just add package app name to appsList
     */
    @Keep
    public static void addDisallowedPackageApplication(String packageName) {
        appsList.add(packageName);
    }

    public static void clearDisallowedPackageApplication() {
        appsList.clear();
    }

    public static void removeDisallowedPackageApplication(String packageName) {
        appsList.remove(packageName);
    }

    public static void addArrayDisallowedPackageApplication(ArrayList<String> packageList) {
        appsList.addAll(packageList);
    }

    private void createNotificationChannel(Context context, String channelID, String channelIDName) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        channelID,
                        channelIDName,
                        NotificationManager.IMPORTANCE_LOW
                );

                serviceChannel.setSound(null, null);
                manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
        } catch (Exception e) {
            Log.e("createNotifiChannel", String.valueOf(e));
            throw new RuntimeException("You have error in [createNotificationChannel]");
        }
    }

}
