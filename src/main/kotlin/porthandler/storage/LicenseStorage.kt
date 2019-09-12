package porthandler.storage

import org.json.JSONObject

object LicenseStorage {
    private var id = 0
    private lateinit var features: JSONObject

    fun getId(): Int = id
    fun getFeatures(): JSONObject = features

    fun setId(id: Int) {
        this.id = id
    }

    fun setFeatures(features: JSONObject) {
        this.features = features
    }
}