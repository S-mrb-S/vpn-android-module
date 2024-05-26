package sp.vpn.module

abstract class VpnActivity : sp.openconnect.remote.CiscoMainActivity() {
    // default cisco options
    override fun isEnableDialog(): Boolean {
        return false // skip dialog (cisco)
    }

    override fun skipCertWarning(): Boolean {
        return true // skip certification warning
    }
}