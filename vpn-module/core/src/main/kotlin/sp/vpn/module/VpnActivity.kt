package sp.vpn.module

abstract class VpnActivity : sp.openconnect.remote.CiscoMainActivity() {
    // default cisco options
    override fun CiscoIsEnableDialog(): Boolean {
        return false // skip dialog (cisco)
    }

    override fun CiscoSkipCertWarning(): Boolean {
        return true // skip certification warning
    }
}