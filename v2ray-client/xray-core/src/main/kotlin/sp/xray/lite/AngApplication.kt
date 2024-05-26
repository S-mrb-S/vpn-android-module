package sp.xray.lite

import android.content.Context
import androidx.multidex.MultiDexApplication
import androidx.work.Configuration
import sp.xray.lite.util.Utils

abstract class AngApplication : MultiDexApplication(), Configuration.Provider {
    companion object {
        //const val PREF_LAST_VERSION = "pref_last_version"
        lateinit var application: AngApplication
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }

    private fun angInit() {
        AppConfig.ANG_PACKAGE = angPackage()
    }

    protected fun setDebugMode(isDebugMode: Boolean) {
        AppConfig.Debug_Mode = isDebugMode
    }

    protected abstract fun mmkvInit()

    /**
     * Application ID
     */
    protected abstract fun angPackage(): String

    //var firstRun = false
    //   private set

    override fun onCreate() {
        super.onCreate()

//        LeakCanary.install(this)

//        val defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
//        firstRun = defaultSharedPreferences.getInt(PREF_LAST_VERSION, 0) != BuildConfig.VERSION_CODE
//        if (firstRun)
//            defaultSharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply()

        //Logger.init().logLevel(if (BuildConfig.DEBUG) LogLevel.FULL else LogLevel.NONE)
        mmkvInit()

        Utils.setNightMode(application)
        angInit()
    }
}
