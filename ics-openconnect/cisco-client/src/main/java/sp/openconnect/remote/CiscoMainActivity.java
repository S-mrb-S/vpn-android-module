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

    protected VpnProfile CiscoVpnProfile;
    protected VPNConnector CiscoConn = null;
    protected OpenVpnService CiscoOpenVpnService;

    protected boolean CiscoCreateProfileWithHostName(@NonNull String hostName){
        assert CiscoConn != null;
        try{
            Log.d("OpenConnect", "CREATE profile from remote: " + hostName);

            hostName = hostName.replaceAll("\\s", "");
            if (!hostName.isEmpty()) {
                FeedbackFragment.recordProfileAdd(this);
                CiscoVpnProfile = sp.openconnect.core.ProfileManager.create(hostName);
            }

        }catch (Exception e){
            return false;
        }
        return true;
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

    protected boolean CiscoStopForceVPN(){
        try{
            CiscoConn.service.stopVPN();
            return true;
        }catch (Exception e){
            Log.d("OpenConnect [err]", e.toString());
        }
        return false;
    }

    protected void CiscoStopOrReconnect(){
        if (CiscoConn.service.getConnectionState() ==
                OpenConnectManagementThread.STATE_DISCONNECTED) {
            CiscoConn.service.startReconnectActivity(this);
        } else {
            CiscoConn.service.stopVPN();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        CiscoConn.unbind();

        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        CiscoUpdateCurrentInfo();
    }

    protected void CiscoUpdateCurrentInfo() {
        Static.isEnableDialog = isEnableDialog();
        Static.CurrentPassWord = CurrentPassWord();
        Static.CurrentUserName = CurrentUserName();
        Static.isSkipCert = skipCertWarning();
    }

    protected abstract String CurrentUserName();
    protected abstract String CurrentPassWord();
    protected abstract boolean isEnableDialog();
    protected abstract void CiscoUpdateUI(OpenVpnService service);
    protected abstract boolean skipCertWarning();

}
