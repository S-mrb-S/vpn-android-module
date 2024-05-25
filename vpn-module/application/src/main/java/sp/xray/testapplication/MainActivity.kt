package sp.xray.testapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sp.openconnect.core.OpenVpnService
import sp.xray.lite.ui.MainActivity
import sp.xray.lite.ui.SubSettingActivity

class MainActivity : sp.vpn.module.VpnActivity() {
    override fun stateV2rayVpn(isRunning: Boolean) {
        Log.d("MRB", "V s: $isRunning")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun setTestStateLayout(content: String) {
        Log.d("MRB", "V: $content")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun sendStatusToCallBack(str: String?, err: Boolean?, errmsg: String?) {
        Log.d("MRB", "O: $str")
    }

    override fun updateConnectionStatus(
        duration: String?,
        lastPacketReceive: String?,
        byteIn: String?,
        byteOut: String?
    ) {
        Log.d("MRB", "O: $duration")
    }

    override fun CurrentUserName(): String {
        return ""
    }

    override fun CurrentPassWord(): String {
        return ""
    }

    override fun CiscoUpdateUI(service: OpenVpnService?) {
        Log.d("MRB", "C: $service")
    }
}