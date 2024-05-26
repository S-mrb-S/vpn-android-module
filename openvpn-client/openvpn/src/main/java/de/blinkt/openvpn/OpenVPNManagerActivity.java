package de.blinkt.openvpn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

/**
 * by MehrabSp
 */
public abstract class OpenVPNManagerActivity extends sp.xray.lite.V2rayControllerActivity {
    private Boolean OpenVpnIsConnected = false;

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

    protected void onClick(ComponentActivity componentActivity, @NotNull String v,
                           @NotNull String config, String password,
                           String username) {
        if (Objects.equals(v, "click")) { // Vpn is running, user would like to disconnect current
            // connection.
            prepareVpn(componentActivity, config, password, username);
        } else if (Objects.equals(v, "force_start")) {// Vpn is running, user would like to
            // disconnect current
            // connection.
            if (OpenVpnStopVpn()) {
                // VPN is stopped, show a Toast message.
                showToast("Disconnect Successfully");
            }

            prepareVpn(componentActivity, config, password, username);

        }
    }

    /**
     * Prepare for vpn connect with required permission
     */
    private void prepareVpn(ComponentActivity componentActivity, String config,
                            String password,
                            String username) {
        if (!OpenVpnIsConnected) {
            // Checking permission for network monitor
            Intent intent = VpnService.prepare(this);

            if (intent != null) {
                try {
                    componentActivity.startActivityForResult(intent, 1);
                } catch (Exception e) {
                    sendStatusToCallBack("VPNSERVICE", true, e.toString());
                    e.printStackTrace();
                }
            } else startVpn(config, password, username);//have already permission

        } else if (OpenVpnStopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
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
        setStatus(OpenVPNService.getStatus());
    }

    /**
     * Start the VPN
     */
    public void startVpn(@NotNull String config, String password, String username) {
        try {
            showToast("Started!");

            OpenVpnApi.startVpn(this, config, "Sweden", username, password);
        } catch (RemoteException e) {
            sendStatusToCallBack("START", true, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Status change with corresponding vpn connection status
     *
     * @param connectionState msg
     */
    public void setStatus(String connectionState) {
        if (connectionState != null)
            sendStatusToCallBack(connectionState);
    }

    /**
     * Receive broadcast message
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String status = intent.getStringExtra("state");
                if(status != null){
                    setStatus(status);
                    if(status.equals("CONNECTED")){
                        OpenVpnIsConnected = true;
                    }
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
     *  default:
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

    /**
     * Change server when user select new server
     */
    protected void newOpenVpnServer(ComponentActivity componentActivity, String config,
                                    String password, String username) {
        // Stop previous connection
        if (OpenVpnIsConnected) {
            OpenVpnStopVpn();
        }

        prepareVpn(componentActivity, config, password, username);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}

