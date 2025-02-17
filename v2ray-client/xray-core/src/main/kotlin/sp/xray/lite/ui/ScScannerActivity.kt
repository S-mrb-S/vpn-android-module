package sp.xray.lite.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.tbruyelle.rxpermissions.RxPermissions
import sp.xray.lite.R
import sp.xray.lite.extension.toast
import sp.xray.lite.util.AngConfigManager

class ScScannerActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_x_none)
        importQRcode()
    }

    fun importQRcode(): Boolean {
        RxPermissions(this)
            .request(Manifest.permission.CAMERA)
            .subscribe {
                if (it)
                    scanQRCode.launch(Intent(this, ScannerActivity::class.java))
                else
                    toast(R.string.toast_permission_denied)
            }

        return true
    }

    private val scanQRCode =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val count = AngConfigManager.importBatchConfig(
                    it.data?.getStringExtra("SCAN_RESULT"),
                    "",
                    false
                )
                if (count > 0) {
                    toast(R.string.toast_success)
                } else {
                    toast(R.string.toast_failure)
                }
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
}
