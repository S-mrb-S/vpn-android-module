package sp.xray.testapplication

import androidx.work.Configuration
import com.tencent.mmkv.MMKV

class Application : sp.vpn.module.VpnApplication() {
    override fun mmkvInit() {
        MMKV.initialize(this)
    }

    override fun angPackage(): String {
        return BuildConfig.APPLICATION_ID
    }

    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:bg")
            .build()
    }

    override fun getContentTitle(): String {
        return "testapplication"
    }

    override fun getChannelID(): String {
        return "sp.xray.testapplication"
    }

    override fun getChannelIDName(): String {
        return "spxraytestapplication"
    }
}