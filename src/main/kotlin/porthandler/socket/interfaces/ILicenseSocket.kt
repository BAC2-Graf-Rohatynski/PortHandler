package porthandler.socket.interfaces

import org.json.JSONArray

interface ILicenseSocket {
    fun send(message: JSONArray)
}