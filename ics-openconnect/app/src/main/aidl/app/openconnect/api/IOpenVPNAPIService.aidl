// IOpenVPNAPIService.aidl
package app.openconnect.api;

import app.openconnect.api.APIVpnProfile;
import app.openconnect.api.IOpenVPNStatusCallback; 

import android.content.Intent;
import android.os.ParcelFileDescriptor;

interface IOpenVPNAPIService {
	List<APIVpnProfile> getProfiles();
	
	void startProfile (String profileUUID);
	
	/** Use a profile with all certificates etc. embedded */
	boolean addVPNProfile (String name, String config);
	
	/** start a profile using an config */
	void startVPN (String inlineconfig);
	
	/** This permission framework is used  to avoid confused deputy style attack to the VPN
	 * calling this will give null if the app is allowed to use the external API and an Intent
	 * that can be launched to request permissions otherwise */
	Intent prepare (String packagename);
	
	/** Used to trigger to the Android VPN permission dialog (VPNService.prepare()) in advance,
	 * if this return null OpenVPN for ANdroid already has the permissions otherwise you can start the returned Intent
	 * to let OpenVPN for Android request the permission */
	Intent prepareVPNService ();

	/* Disconnect the VPN */
    void disconnect();
    
    /**
      * Registers to receive OpenVPN Status Updates
      */
    void registerStatusCallback(IOpenVPNStatusCallback cb);
    
    /**
     * Remove a previously registered callback interface.
     */
    void unregisterStatusCallback(IOpenVPNStatusCallback cb);
		
}