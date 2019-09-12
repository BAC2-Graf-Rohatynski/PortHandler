package porthandler.comport.interfaces

import org.json.JSONObject
import porthandler.storage.LicenseStorage

interface IComPortThread {
    fun restartAfterLicenseChange()
    fun getLicenseFeature(): JSONObject = LicenseStorage.getFeatures()
}