package sp.xray.testapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sp.openconnect.core.OpenVpnService

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
        Toast.makeText(this@MainActivity, "s 1", Toast.LENGTH_SHORT).show()

        GlobalScope.launch {
            delay(5000)

            // test v2ray (success)
            this@MainActivity.runOnUiThread {
                Toast.makeText(this@MainActivity, "s 2", Toast.LENGTH_SHORT).show()
                this@MainActivity.addAndConnectV2ray("vless://dd8ef5af-5940-2c1f-396e-fcafdf97414c@81.12.92.130:15404?security=&alpn=h2,http/1.1&fp=random&type=tcp&path=/&headerType=http&host=&encryption=none#%F0%9F%87%B3%F0%9F%87%B1%20@iProxyRobot%20[NL-TUN-3x]")
            }
        }

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