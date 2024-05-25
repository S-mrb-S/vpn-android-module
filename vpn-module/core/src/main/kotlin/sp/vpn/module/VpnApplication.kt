package sp.vpn.module

import androidx.work.Configuration
import com.tencent.mmkv.MMKV

abstract class VpnApplication : sp.openconnect.Application() {
    override fun mmkvInit() {
        MMKV.initialize(this)
    }

}