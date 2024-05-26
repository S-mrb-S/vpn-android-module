package sp.openconnect.remote;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import sp.openconnect.VpnProfile;
import sp.openconnect.api.GrantPermissionsActivity;
import sp.openconnect.core.OpenConnectManagementThread;
import sp.openconnect.core.OpenVpnService;
import sp.openconnect.core.VPNConnector;
import sp.openconnect.fragments.FeedbackFragment;

/*
    by MehrabSp
 */
public abstract class CiscoMainActivity extends de.blinkt.openvpn.OpenVPNManagerActivity {

    private VpnProfile CiscoVpnProfile;
    private VPNConnector CiscoConn = null;
    // protected OpenVpnService CiscoOpenVpnService;

    /**
     * Create profile and start or stopVpn
     */
    protected void CiscoFabClick(@NonNull String hostName) throws Exception {
        if(!CiscoStopVPN())
        {
            if(CiscoCreateProfileWithHostName(hostName))
            {
                CiscoStartVPNWithProfile();
            }
        }
    }

    protected boolean CiscoCreateProfileWithHostName(@NonNull String hostName) throws Exception {
        assert CiscoConn != null;
        try{
            Log.d("OpenConnect", "CREATE profile from remote: " + hostName);

            hostName = hostName.replaceAll("\\s", "");
            if (!hostName.isEmpty()) {
                FeedbackFragment.recordProfileAdd(this);
                CiscoVpnProfile = sp.openconnect.core.ProfileManager.create(hostName);
            }

            return true;
        }catch (Exception e){
            throw new Exception("Error when create Cisco profile: " + e);
        }
    }

    protected void CiscoStartVPNWithProfile(){
        assert CiscoVpnProfile != null;

        Intent intent = new Intent(this, GrantPermissionsActivity.class);
        String pkg = this.getPackageName();

        intent.putExtra(pkg + GrantPermissionsActivity.EXTRA_UUID,
                CiscoVpnProfile.getUUID().toString());
        intent.setAction(Intent.ACTION_MAIN);
        this.startActivity(intent);
    }

    protected boolean CiscoStopVPN(){
        try{
            if (CiscoConn.service.getConnectionState() !=
                    OpenConnectManagementThread.STATE_DISCONNECTED) {
                CiscoConn.service.stopVPN();
                return true;
            }
        }catch (Exception e){
            Log.d("OpenConnect [err]", e.toString());
        }
        return false;
    }

    /**
     * Reconnect current profile or StopVpn
     */
    protected void CiscoReconnectVpn(){
        CiscoStopVPN();
        CiscoConn.service.startReconnectActivity(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Don't move to onResume
        CiscoConn = new VPNConnector(this, true) { // run on activity (require)
            @Override
            public void onUpdate(OpenVpnService service) {
                CiscoUpdateUI(service);
            }
        };
    }

    @Override
    public void onStop() {
        CiscoConn.stopActiveDialog();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        CiscoConn.unbind(); // don't move to onStop

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        CiscoUpdateCurrentInfo();
    }

    protected void CiscoUpdateCurrentInfo() {
        Static.isEnableDialog = CiscoIsEnableDialog();
        Static.CurrentPassWord = CiscoCurrentPassWord();
        Static.CurrentUserName = CiscoCurrentUserName();
        Static.isSkipCert = CiscoSkipCertWarning();
    }

    protected abstract String CiscoCurrentUserName();
    protected abstract String CiscoCurrentPassWord();
    protected abstract boolean CiscoIsEnableDialog();
    protected abstract void CiscoUpdateUI(OpenVpnService service);
    protected abstract boolean CiscoSkipCertWarning();

}
