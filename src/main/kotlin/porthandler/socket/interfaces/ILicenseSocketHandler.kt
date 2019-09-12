package porthandler.socket.interfaces

import apibuilder.license.response.ResponseItem

interface ILicenseSocketHandler {
    fun parseLicenseInformation(response: ResponseItem)
}