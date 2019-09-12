package porthandler.artnet

import enumstorage.port.SerialPorts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.artnet.interfaces.IArtnetThread

object ArtnetThread: IArtnetThread {
    private lateinit var artnetHandler: ArtnetHandler
    private val logger: Logger = LoggerFactory.getLogger(ArtnetThread::class.java)

    init {
        try {
            logger.info("Initializing Artnet handler ...")
            initialization()
        } catch (ex: Exception) {
            logger.error("Error occurred while running COM port handler!\n${ex.message}")
        }
    }

    @Synchronized
    override fun restartAfterLicenseChange() {
        try {
            logger.info("Restarting Artnet ports after license change ...")
            dumpArtnetHandler()
            initialization()
        } catch (ex: Exception) {
            logger.error("Error while restarting Artnet ports!\n${ex.message}")
        }
    }

    private fun dumpArtnetHandler() {
        if (::artnetHandler.isInitialized) {
            artnetHandler.closeSockets()
        }
    }

    private fun initialization() {
        if (checkArtnetFeature()) {
            artnetHandler = ArtnetHandler()
            artnetHandler.start()
        } else {
            logger.warn("License feature not enabled for Artnet")
        }
    }

    private fun checkArtnetFeature(): Boolean = getLicenseFeature().getString(SerialPorts.Artnet.name) == "1"
}