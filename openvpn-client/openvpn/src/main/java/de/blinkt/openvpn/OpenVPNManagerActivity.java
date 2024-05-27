package de.blinkt.openvpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * by MehrabSp
 */
public abstract class OpenVPNManagerActivity extends sp.xray.lite.V2rayControllerActivity {
    private Boolean OpenVpnIsConnected = false;

    public OpenVPNManagerActivity(@NonNull String getDirectUrlV2ray, boolean getShowSpeedBooleanV2ray) {
        super(getDirectUrlV2ray, getShowSpeedBooleanV2ray);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(this.getCacheDir());

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("connectionState"));
    }

    private void sendStatusToCallBack(String str) {
        OpenVpnStatus(str, false, null);
    }

    private void sendStatusToCallBack(String str, Boolean err, String msg) {
        OpenVpnStatus(str, err, msg);
    }

    protected abstract void OpenVpnStatus(String str, Boolean err, String errmsg);

    /**
     * Prepare for vpn connect with required permission
     */
    private String configC, passwordC, usernameC;

    @Override
    protected void getResultOpenVpn() {
        OpenVpnStartVpn(configC, passwordC, usernameC);
    }

    protected void OpenVpnFabClick(String config, String password, String username) {
        if (!OpenVpnStopVpn()) {
            configC = config;
            passwordC = password;
            usernameC = username;
            sendRequestOpenVpnPermission();
        }
    }

    /**
     * Stop vpn
     *
     * @return boolean: VPN status
     */
    protected boolean OpenVpnStopVpn() {
        try {
            if (OpenVpnIsConnected) {
                OpenVPNThread.stop();
                return true;
            }
        } catch (Exception e) {
            sendStatusToCallBack("STOPTHREAD", true, e.toString());
        }
        return false;
    }

    /**
     * Get service status
     */
    public void isServiceRunning() {
        sendStatusToCallBack(OpenVPNService.getStatus());
    }

    /**
     * Start the VPN
     */
    protected void OpenVpnStartVpn(@NotNull String config, String password, String username) {
        try {
            showToast("Started!");

            OpenVpnApi.startVpn(this, config, "Sweden", username, password);
        } catch (RemoteException e) {
            sendStatusToCallBack("START", true, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Receive broadcast message
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String status = intent.getStringExtra("state");
                if (status != null) {
                    sendStatusToCallBack(status);
                    switch (status) {
                        case "DISCONNECTED":
                            OpenVpnIsConnected = false;
                            break;

                        default:
                            OpenVpnIsConnected = true;
                            break;
                    }
//                    if (!status.equals("DISCONNECTED")) {
//                        OpenVpnIsConnected = true;
//                    }
                }
            } catch (Exception e) {
                sendStatusToCallBack("SENDSTATUS", true, e.toString());
                e.printStackTrace();
            }

            try {
                String duration = intent.getStringExtra("duration");
                String lastPacketReceive = intent.getStringExtra("lastPacketReceive");
                String byteIn = intent.getStringExtra("byteIn");
                String byteOut = intent.getStringExtra("byteOut");

                if (duration == null) duration = "00:00:00";
                if (lastPacketReceive == null) lastPacketReceive = "0";
                if (byteIn == null) byteIn = " ";
                if (byteOut == null) byteOut = " ";
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut);
            } catch (Exception e) {
                sendStatusToCallBack("STATUS", true, e.toString());
                e.printStackTrace();
            }

        }
    };

    /**
     * Update status UI
     * default:
     *
     * @param duration:          running time --> 00:00:00
     * @param lastPacketReceive: last packet receive time --> 0
     * @param byteIn:            incoming data --> null
     * @param byteOut:           outgoing data --> null
     */
    protected abstract void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut);

    /**
     * Show toast message
     *
     * @param message: toast message
     */
    public void showToast(String message) {
        if (App.isShowToastOpenVpn)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}

