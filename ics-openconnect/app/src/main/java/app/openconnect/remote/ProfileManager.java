package app.openconnect.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import app.openconnect.VpnProfile;
import app.openconnect.api.GrantPermissionsActivity;
import app.openconnect.core.OpenConnectManagementThread;
import app.openconnect.core.OpenVpnService;
import app.openconnect.core.VPNConnector;
import app.openconnect.fragments.FeedbackFragment;
import app.openconnect.remote.data.Static;

/**
 * by MehrabSp
 * Java 11
 */
public class ProfileManager {
    public VpnProfile vpnProfile;
    private final Context context = Static.getGlobalData().getMainApplication();
    private final VPNConnector mConn;
    private OpenVpnService openVpnService;

    /**
     * first set context on ./data/Static
     * for start vpn first CreateProfileWithName
     */
    public ProfileManager(){
        mConn = new VPNConnector(context, false) {
            @Override
            public void onUpdate(OpenVpnService service) {
                openVpnService = service;
            }
        };
    }

    /**
     * Status!
     * @return Example: getUpdateUI().getConnectionState() == OpenConnectManagementThread
     * .STATE_CONNECTED: means Connected!
     */
    public OpenVpnService getUpdateUI(){
        return this.openVpnService;
    }

    /**
     * put on -->
     * Fragment: onDestroyView
     * Activity: onStop or ..
     */
    public void unBindUpdateUI(){
        this.mConn.unbind();
    }

    /**
     * You don't need this
     * @return Get current service
     */
    public VPNConnector getVpnConnector(){
        return this.mConn;
    }

    public boolean CreateProfileWithName(@NonNull String hostName){
        assert context != null;
        try{
            Log.d("OpenConnect", "CREATE profile from remote: " + hostName);

            hostName = hostName.replaceAll("\\s", "");
            if (!hostName.isEmpty()) {
                FeedbackFragment.recordProfileAdd(context);
                vpnProfile = app.openconnect.core.ProfileManager.create(hostName);
            }

        }catch (Exception e){
            return false;
        }
        return true;
    }

    public void StartVPNWithProfile(){
        assert vpnProfile != null;

        Intent intent = new Intent(context, GrantPermissionsActivity.class);
        String pkg = context.getPackageName();

        intent.putExtra(pkg + GrantPermissionsActivity.EXTRA_UUID,
                vpnProfile.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        context.startActivity(intent);
    }

    public void stopForceVPN(){
        if (mConn.service.getConnectionState() ==
                OpenConnectManagementThread.STATE_DISCONNECTED) {
            mConn.service.startReconnectActivity(context);
        } else {
            mConn.service.stopVPN();
        }
    }

    public void StopOrReconnect(){
        if (mConn.service.getConnectionState() ==
                OpenConnectManagementThread.STATE_DISCONNECTED) {
            mConn.service.startReconnectActivity(context);
        } else {
            mConn.service.stopVPN();
        }
    }

}
