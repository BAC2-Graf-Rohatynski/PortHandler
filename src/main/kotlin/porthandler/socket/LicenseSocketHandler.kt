package porthandler.socket

import apibuilder.license.response.ResponseItem
import apibuilder.port.ActiveLicenseItem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.PortHandlerRunner
import porthandler.artnet.ArtnetThread
import porthandler.comport.ComPortThread
import porthandler.socket.interfaces.ILicenseSocketHandler
import porthandler.storage.LicenseStorage
import propertystorage.WatchdogProperties
import java.lang.Exception
import kotlin.concurrent.thread

object LicenseSocketHandler: ILicenseSocketHandler {
    private val logger: Logger = LoggerFactory.getLogger(LicenseSocketHandler::class.java)
    private lateinit var licenseSocket: LicenseSocket
    private val requestTimeout = WatchdogProperties.getLicenseRequestTimeout()

    init {
        connect()
        requestLicenseInformation()
    }

    @Synchronized
    override fun parseLicenseInformation(response: ResponseItem) {
        logger.info("Parsing incoming license information '${response.toJson()}' ...")
        val license = response.licenses.first()

        if (LicenseStorage.getId() != license.id) {
            logger.info("Updating license id ...")
            LicenseStorage.setId(id = license.id)
        }

        if (LicenseStorage.getFeatures() != license.features) {
            logger.info("Updating license feature ...")
            LicenseStorage.setFeatures(features = license.features)
            ComPortThread.restartAfterLicenseChange()
            ArtnetThread.restartAfterLicenseChange()
        }
    }

    private fun requestLicenseInformation() {
        thread {
            val message = ActiveLicenseItem().create().build()

            while (PortHandlerRunner.isRunnable()) {
                try {
                    logger.info("Requesting active license from license server ...")
                    licenseSocket.send(message = message)
                    Thread.sleep(requestTimeout)
                } catch (ex: Exception) {
                    logger.error("Error occurred while requesting license information!\n${ex.message}")
                }
            }
        }
    }

    private fun connect() {
        try {
            logger.info("Connecting ...")
            licenseSocket = LicenseSocket()
            logger.info("Connected")
        } catch (ex: Exception) {
            logger.error("Error occurred while connecting!\n${ex.message}")
        }
    }
}