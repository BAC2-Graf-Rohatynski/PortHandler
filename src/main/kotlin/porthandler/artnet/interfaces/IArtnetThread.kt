package porthandler.artnet.interfaces

import org.json.JSONObject
import porthandler.storage.LicenseStorage

interface IArtnetThread {
    fun restartAfterLicenseChange()
    fun getLicenseFeature(): JSONObject = LicenseStorage.getFeatures()
}