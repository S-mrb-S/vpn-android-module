package sp.xray.testapplication

import androidx.work.Configuration
import com.tencent.mmkv.MMKV
import sp.xray.lite.AngApplication

class Application : AngApplication() {
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
}