package sp.xray.lite.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sp.xray.lite.util.MyContextWrapper
import sp.xray.lite.util.Utils

abstract class BaseActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let {
            MyContextWrapper.wrap(newBase, Utils.getLocale(newBase))
        }
        super.attachBaseContext(context)
    }
}
