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

import de.blinkt.openvpn.core.App;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.OpenVPNStatusService;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.VpnStatus;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * by MehrabSp
 */
public abstract class OpenVPNManagerActivity extends sp.xray.lite.V2rayControllerActivity {
    protected OpenVPNThread vpnThread = new OpenVPNThread();
    protected OpenVPNService vpnService = new OpenVPNService();
    protected String OpenVpnStatus = "";
    protected Boolean isOpenVpnALive = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking is vpn already running or not
        isServiceRunning();
        VpnStatus.initLogCache(this.getCacheDir());

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("connectionState"));
    }

    private void sendStatusToCallBack(String str){
        sendStatusToCallBack(str, false, null);
    }
    protected abstract void sendStatusToCallBack(String str, Boolean err, String errmsg);

    /**
     * @param v: click listener view
     */
    public void onClick(@NotNull String v) {
        if (Objects.equals(v, "force_stop")) {// Vpn is running, user would like to
            // disconnect current connection.
            stopVpn();
        }
    }

    protected void onClick(ComponentActivity componentActivity, @NotNull String v,
                        @NotNull String config, String password,
                        String username) {
        if (Objects.equals(v, "click")) {// Vpn is running, user would like to disconnect current
            // connection.
            prepareVpn(componentActivity, config, password, username);
        } else if (Objects.equals(v, "force_start")) {// Vpn is running, user would like to
            // disconnect current
            // connection.
            if (stopVpn()) {
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
        if (!isOpenVpnALive) {
            // Checking permission for network monitor
            Intent intent = VpnService.prepare(this);

            if (intent != null) {
                try{
                    componentActivity.startActivityForResult(intent, 1);
                }catch (Exception e){
                    sendStatusToCallBack("VPNSERVICE", true, e.toString());
                    e.printStackTrace();
                }
            } else startVpn(config, password, username);//have already permission

        } else if (stopVpn()) {

            // VPN is stopped, show a Toast message.
            showToast("Disconnect Successfully");
        }
    }

    /**
     * Stop vpn
     *
     * @return boolean: VPN status
     */
    public boolean stopVpn() {
        try {
            OpenVPNThread.stop();
            return true;
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
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String status = intent.getStringExtra("state");
                setStatus(status);
                OpenVpnStatus = status; // no need
                //                    case "CONNECTING":
                //                        return R.string.state_connecting;
                //                    case "WAIT":
                //                        return R.string.state_wait;
                //                    case "AUTH":
                //                        return R.string.state_auth;
                //                    case "GET_CONFIG":
                //                        return R.string.state_get_config;
                //                    case "ASSIGN_IP":
                //                        return R.string.state_assign_ip;
                //                    case "ADD_ROUTES":
                //                        return R.string.state_add_routes;
                //                    case "DISCONNECTED":
                //                        return R.string.state_disconnected;
                //                    case "RECONNECTING":
                //                        return R.string.state_reconnecting;
                //                    case "EXITING":
                //                        return R.string.state_exiting;
                //                    case "RESOLVE":
                //                        return R.string.state_resolve;
                //                    case "TCP_CONNECT":
                //                        return R.string.state_tcp_connect;
                //                    case "AUTH_PENDING":
                //                        return R.string.state_auth_pending;
                // ... and more
                isOpenVpnALive = Objects.requireNonNull(status).equals("CONNECTED");

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
     *
     * @param duration:          running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn:            incoming data
     * @param byteOut:           outgoing data
     */
    protected abstract void updateConnectionStatus(String duration, String lastPacketReceive, String byteIn, String byteOut);

    /**
     * Show toast message
     *
     * @param message: toast message
     */
    public void showToast(String message) {
        if(App.isShowToastOpenVpn)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Change server when user select new server
     */
    protected void newOpenVpnServer(ComponentActivity componentActivity, String config,
                                 String password, String username) {
        // Stop previous connection
        if (isOpenVpnALive) {
            stopVpn();
        }

        prepareVpn(componentActivity, config, password, username);
    }
    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

}

