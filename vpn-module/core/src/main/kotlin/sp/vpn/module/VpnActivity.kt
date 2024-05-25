package sp.vpn.module

abstract class VpnActivity : sp.openconnect.remote.CiscoMainActivity() {
    override fun isEnableDialog(): Boolean {
        return false
    }

    override fun skipCertWarning(): Boolean {
        return true
    }
}