package sp.vpn.module

abstract class VpnActivity(getDirectUrlV2ray: String, getShowSpeedBooleanV2ray: Boolean) : sp.openconnect.remote.CiscoMainActivity(
    getDirectUrlV2ray, getShowSpeedBooleanV2ray
) {
    // default cisco options
    override fun CiscoIsEnableDialog(): Boolean {
        return false // skip dialog (cisco)
    }

    override fun CiscoSkipCertWarning(): Boolean {
        return true // skip certification warning
    }
}