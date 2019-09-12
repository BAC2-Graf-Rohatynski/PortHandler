package porthandler

import at.nlc.error.ErrorClientRunner
import enumstorage.update.ApplicationName
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.artnet.ArtnetThread
import porthandler.comport.ComPortThread
import porthandler.socket.LicenseSocketHandler

object PortHandlerRunner {
    private val logger: Logger = LoggerFactory.getLogger(PortHandlerRunner::class.java)

    @Volatile
    private var runApplication = true

    fun start() {
        logger.info("Starting application")
        ErrorClientRunner
        LicenseSocketHandler
        ComPortThread
        ArtnetThread
    }

    @Synchronized
    fun isRunnable(): Boolean = runApplication

    fun stop() {
        logger.info("Stopping application")
        runApplication = false

        ErrorClientRunner.stop()
    }

    fun getUpdateInformation(): JSONObject = UpdateInformation.getAsJson(applicationName = ApplicationName.Port.name)
}