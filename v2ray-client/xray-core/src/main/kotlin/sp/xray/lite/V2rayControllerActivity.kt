package sp.xray.lite

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.drakeet.support.toast.ToastCompat
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import sp.xray.lite.AppConfig.ANG_PACKAGE
import sp.xray.lite.dto.EConfigType
import sp.xray.lite.extension.toast
import sp.xray.lite.service.V2RayServiceManager
import sp.xray.lite.ui.AboutActivity
import sp.xray.lite.ui.BaseActivity
import sp.xray.lite.ui.LogcatActivity
import sp.xray.lite.ui.RoutingSettingsFragment
import sp.xray.lite.ui.ScannerActivity
import sp.xray.lite.ui.ServerActivity
import sp.xray.lite.ui.SettingsActivity
import sp.xray.lite.ui.SubSettingActivity
import sp.xray.lite.ui.UserAssetActivity
import sp.xray.lite.util.AngConfigManager
import sp.xray.lite.util.MmkvManager
import sp.xray.lite.util.Utils
import sp.xray.lite.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * by MRB
 */
abstract class V2rayControllerActivity : BaseActivity() {

    private val mainStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_MAIN,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val settingsStorage by lazy {
        MMKV.mmkvWithID(
            MmkvManager.ID_SETTING,
            MMKV.MULTI_PROCESS_MODE
        )
    }
    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val requestCode = it.data?.getIntExtra("REQUEST_CODE", -1)
                if (requestCode == 5) {
                    getResultOpenVpn()
                } else {
                    V2RayStart()
                }
            }
        }

    // for OpenVpn
    protected fun sendRequestOpenVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent == null) {
            getResultOpenVpn()
        } else {
            intent.putExtra("REQUEST_CODE", 5)
            requestVpnPermission.launch(intent)
        }
    }

    protected abstract fun getResultOpenVpn()

    // v2ray options setting
    protected var V2rayShowSpeedNotif: Boolean = true
    protected var V2rayDirectURLORIP: String = ""

    private val mainViewModel: MainViewModel by viewModels()

    protected fun V2rayFabClick(config: String) {
        delAndAddV2rayConfig(config)
        if(!V2rayStop()){
            if ((settingsStorage?.decodeString(AppConfig.PREF_MODE) ?: "VPN") == "VPN") {
                val intent = VpnService.prepare(this)
                if (intent == null) {
                    V2RayStart()
                } else {
                    requestVpnPermission.launch(intent)
                }
            } else {
                V2RayStart()
            }
        }
    }

    /**
     * send VpnService permissions, return false means permissions denied
     *میتوانید قبل از وصل شدن به هر اتصالی اول مجوز بگیرید.
     * @return true --> مجوز دارید
     */
    protected fun forceInitVpnService(): Boolean {
        val intent = VpnService.prepare(this)
        return intent == null
    }

    protected fun layoutTestClick() {
        if (mainViewModel.isRunning.value == true) {
            setTestStateLayout(getString(R.string.connection_test_testing))
            mainViewModel.testCurrentServerRealPing()
        } else {
            setTestStateLayout("No connection")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        copyAssets()
        //migrateLegacy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RxPermissions(this)
                .request(Manifest.permission.POST_NOTIFICATIONS)
                .subscribe {
                    if (!it)
                        toast(R.string.toast_permission_denied)
                }
        }

        if(settingsStorage?.decodeBool(AppConfig.PREF_SPEED_ENABLED) != V2rayShowSpeedNotif){
            settingsStorage?.encode(AppConfig.PREF_SPEED_ENABLED, V2rayShowSpeedNotif)
        }
        if(settingsStorage?.decodeString(AppConfig.PREF_V2RAY_ROUTING_DIRECT) != V2rayDirectURLORIP) {
            settingsStorage?.encode(AppConfig.PREF_V2RAY_ROUTING_DIRECT, V2rayDirectURLORIP)
        }
    }

    protected abstract fun setTestStateLayout(content: String)
    protected abstract fun stateV2rayVpn(isRunning: Boolean)

    private fun setupViewModel() {
        mainViewModel.updateTestResultAction.observe(this) { setTestStateLayout(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            stateV2rayVpn(isRunning)
        }
        mainViewModel.startListenBroadcast()
    }

    private fun copyAssets() {
        val extFolder = Utils.userAssetPath(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geo = arrayOf("geosite.dat", "geoip.dat")
                assets.list("")
                    ?.filter { geo.contains(it) }
                    ?.filter { !File(extFolder, it).exists() }
                    ?.forEach {
                        val target = File(extFolder, it)
                        assets.open(it).use { input ->
                            FileOutputStream(target).use { output ->
                                input.copyTo(output)
                            }
                        }
                        Log.i(
                            ANG_PACKAGE,
                            "Copied from apk assets folder to ${target.absolutePath}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(ANG_PACKAGE, "asset copy failed", e)
            }
        }
    }

//    private fun migrateLegacy() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val result = AngConfigManager.migrateLegacyConfig(this@MainActivity)
//            if (result != null) {
//                launch(Dispatchers.Main) {
//                    if (result) {
//                        toast(getString(R.string.migration_success))
//                        mainViewModel.reloadServerList()
//                    } else {
//                        toast(getString(R.string.migration_fail))
//                    }
//                }
//            }
//        }
//    }

    protected fun V2RayStart() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            return
        }
        V2RayServiceManager.startV2Ray(this)
    }

    protected fun V2rayStop(): Boolean {
        try{
            if (mainViewModel.isRunning.value == true) {
                Utils.stopVService(this)
                return true
            }
        }catch (e: Exception){
            Log.d("V2ray err", e.toString())
        }
        return false
    }

    protected fun V2RayRestart() {
        V2rayStop()
        Observable.timer(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                V2RayStart()
            }
    }

    protected fun importQrcode() {
        importQRcode(true)
    }

    protected fun importManuallyVmess() {
        importManually(EConfigType.VMESS.value)
    }

    protected fun importManuallyVless() {
        importManually(EConfigType.VLESS.value)
    }

    protected fun importManuallySs() {
        importManually(EConfigType.SHADOWSOCKS.value)
    }

    protected fun importManuallySocks() {
        importManually(EConfigType.SOCKS.value)
    }

    protected fun importManuallyTrojan() {
        importManually(EConfigType.TROJAN.value)
    }

    protected fun importManuallyWireguard() {
        importManually(EConfigType.WIREGUARD.value)
    }

    protected fun importConfigCustomUrl() {
        importConfigCustomUrlClipboard()
    }

    protected fun importConfigCustomUrlScan() {
        importQRcode(false)
    }

    protected fun subUpdate() {
        importConfigViaSub()
    }

    @Deprecated("No effect")
    protected fun exportAll() {
        if (AngConfigManager.shareNonCustomConfigsToClipboard(
                this,
                mainViewModel.serverList
            ) == 0
        ) {
            toast(R.string.toast_success)
        } else {
            toast(R.string.toast_failure)
        }
    }

    protected fun pingAll() {
        mainViewModel.testAllTcping()
    }

    protected fun realPingAll() {
        mainViewModel.testAllRealPing()
    }

    protected fun delAndAddV2rayConfig(AConfig: String): Boolean {
        try {
            MmkvManager.removeAllServer()
            mainViewModel.reloadServerList()
        } catch (ignore: Exception) {
//            return false
        }

        importBatchConfig(AConfig)
        return true
    }

    @Deprecated("No effect")
    protected fun delAllConfig() {
        AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                MmkvManager.removeAllServer()
                mainViewModel.reloadServerList()
            }
            .setNegativeButton(android.R.string.no) { _, _ ->
                //do noting
            }
            .show()
    }

    protected fun delDuplicateConfig() {
        AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                mainViewModel.removeDuplicateServer()
            }
            .setNegativeButton(android.R.string.no) { _, _ ->
                //do noting
            }
            .show()
    }

    @Deprecated("No effect")
    protected fun delInvalidConfig() {
        AlertDialog.Builder(this).setMessage(R.string.del_config_comfirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                MmkvManager.removeInvalidServer()
                mainViewModel.reloadServerList()
            }
            .setNegativeButton(android.R.string.no) { _, _ ->
                //do noting
            }
            .show()
    }

    @Deprecated("No effect")
    protected fun sortByTestResults() {
        MmkvManager.sortByTestResults()
        mainViewModel.reloadServerList()
    }

    protected fun filterConfig() {
        mainViewModel.filterConfig(this)
    }

    private fun importManually(createConfigType: Int) {
        startActivity(
            Intent()
                .putExtra("createConfigType", createConfigType)
                .putExtra("subscriptionId", mainViewModel.subscriptionId)
                .setClass(this, ServerActivity::class.java)
        )
    }

    /**
     * import config from qrcode
     */
    private fun importQRcode(forConfig: Boolean): Boolean {
//        try {
//            startActivityForResult(Intent("com.google.zxing.client.android.SCAN")
//                    .addCategory(Intent.CATEGORY_DEFAULT)
//                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), requestCode)
//        } catch (e: Exception) {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe {
                if (it)
                    if (forConfig)
                        scanQRCodeForConfig.launch(Intent(this, ScannerActivity::class.java))
                    else
                        scanQRCodeForUrlToCustomConfig.launch(
                            Intent(
                                this,
                                ScannerActivity::class.java
                            )
                        )
                else
                    toast(R.string.toast_permission_denied)
            }
//        }
        return true
    }

    private val scanQRCodeForConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                importBatchConfig(it.data?.getStringExtra("SCAN_RESULT"))
            }
        }

    private val scanQRCodeForUrlToCustomConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                importConfigCustomUrl(it.data?.getStringExtra("SCAN_RESULT"))
            }
        }

    /**
     * import config from clipboard
     */
    protected fun importClipboard()
            : Boolean {
        try {
            val clipboard = Utils.getClipboard(this)
            importBatchConfig(clipboard)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun importBatchConfig(server: String?, subid: String = "") {
        val subid2 = if (subid.isNullOrEmpty()) {
            mainViewModel.subscriptionId
        } else {
            subid
        }
        val append = subid.isNullOrEmpty()

        var count = AngConfigManager.importBatchConfig(server, subid2, append)
        if (count <= 0) {
            count = AngConfigManager.importBatchConfig(Utils.decode(server!!), subid2, append)
        }
        if (count <= 0) {
            count = AngConfigManager.appendCustomConfigServer(server, subid2)
        }
        if (count > 0) {
            toast(R.string.toast_success)
            mainViewModel.reloadServerList()
        } else {
            toast(R.string.toast_failure)
        }
    }

    protected fun importConfigCustomClipboard()
            : Boolean {
        try {
            val configText = Utils.getClipboard(this)
            if (TextUtils.isEmpty(configText)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            importCustomizeConfig(configText)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from local config file
     */
    protected fun importConfigCustomLocal(): Boolean {
        try {
            showFileChooser()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun importConfigCustomUrlClipboard()
            : Boolean {
        try {
            val url = Utils.getClipboard(this)
            if (TextUtils.isEmpty(url)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            return importConfigCustomUrl(url)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from url
     */
    private fun importConfigCustomUrl(url: String?): Boolean {
        try {
            if (!Utils.isValidUrl(url)) {
                toast(R.string.toast_invalid_url)
                return false
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val configText = try {
                    Utils.getUrlContentWithCustomUserAgent(url)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
                launch(Dispatchers.Main) {
                    importCustomizeConfig(configText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * import config from sub
     */
    private fun importConfigViaSub()
            : Boolean {
        try {
            toast(R.string.title_sub_update)
            MmkvManager.decodeSubscriptions().forEach {
                if (TextUtils.isEmpty(it.first)
                    || TextUtils.isEmpty(it.second.remarks)
                    || TextUtils.isEmpty(it.second.url)
                ) {
                    return@forEach
                }
                if (!it.second.enabled) {
                    return@forEach
                }
                val url = Utils.idnToASCII(it.second.url)
                if (!Utils.isValidUrl(url)) {
                    return@forEach
                }
                Log.d(ANG_PACKAGE, url)
                lifecycleScope.launch(Dispatchers.IO) {
                    var configText = try {
                        Utils.getUrlContentWithCustomUserAgent(url)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ""
                    }
                    if (configText.isEmpty()) {
                        configText = try {
                            val httpPort = Utils.parseInt(
                                settingsStorage?.decodeString(AppConfig.PREF_HTTP_PORT),
                                AppConfig.PORT_HTTP.toInt()
                            )
                            Utils.getUrlContentWithCustomUserAgent(url, httpPort)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                    }
                    if (configText.isEmpty()) {
                        launch(Dispatchers.Main) {
                            toast("\"" + it.second.remarks + "\" " + getString(R.string.toast_failure))
                        }
                        return@launch
                    }
                    launch(Dispatchers.Main) {
                        importBatchConfig(configText, it.first)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * show file chooser
     */
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            chooseFileForCustomConfig.launch(
                Intent.createChooser(
                    intent,
                    getString(R.string.title_file_chooser)
                )
            )
        } catch (ex: ActivityNotFoundException) {
            toast(R.string.toast_require_file_manager)
        }
    }

    private val chooseFileForCustomConfig =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val uri = it.data?.data
            if (it.resultCode == RESULT_OK && uri != null) {
                readContentFromUri(uri)
            }
        }

    /**
     * read content from uri
     */
    private fun readContentFromUri(uri: Uri) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        RxPermissions(this)
            .request(permission)
            .subscribe {
                if (it) {
                    try {
                        contentResolver.openInputStream(uri).use { input ->
                            importCustomizeConfig(input?.bufferedReader()?.readText())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else
                    toast(R.string.toast_permission_denied)
            }
    }

    /**
     * import customize config
     */
    private fun importCustomizeConfig(server: String?) {
        try {
            if (server == null || TextUtils.isEmpty(server)) {
                toast(R.string.toast_none_data)
                return
            }
            mainViewModel.appendCustomConfigServer(server)
            mainViewModel.reloadServerList()
            toast(R.string.toast_success)
            //adapter.notifyItemInserted(mainViewModel.serverList.lastIndex)
        } catch (e: Exception) {
            ToastCompat.makeText(
                this,
                "${getString(R.string.toast_malformed_josn)} ${e.cause?.message}",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
            return
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun onNavigationItemSelected(item: Int): Boolean {
        // Handle navigation view item clicks here.
        when (item) {
            //R.id.server_profile -> activityClass = MainActivity::class.java
            R.id.sub_setting -> {
                startActivity(Intent(this, SubSettingActivity::class.java))
            }

            R.id.settings -> {
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                        .putExtra("isRunning", mainViewModel.isRunning.value == true)
                )
            }

            R.id.user_asset_setting -> {
                startActivity(Intent(this, UserAssetActivity::class.java))
            }

            R.id.promotion -> {
                Utils.openUri(
                    this,
                    "${Utils.decode(AppConfig.PromotionUrl)}?t=${System.currentTimeMillis()}"
                )
            }

            R.id.logcat -> {
                startActivity(Intent(this, LogcatActivity::class.java))
            }

            R.id.about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        return true
    }
}
