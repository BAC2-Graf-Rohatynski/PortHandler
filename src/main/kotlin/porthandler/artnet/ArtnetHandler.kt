package porthandler.artnet

import ch.bildspur.artnet.ArtNetClient
import enumstorage.port.SerialPorts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.PortHandlerRunner
import porthandler.artnet.interfaces.IArtnetHandler
import porthandler.sender.NlcSender
import propertystorage.InterfaceProperties
import java.net.InetAddress
import java.net.NetworkInterface

class ArtnetHandler: IArtnetHandler, Thread() {
    private lateinit var artnetClient: ArtNetClient
    private val numberOfUniverses = 32
    private val numberOfChannels = 512
    private val sender: NlcSender = NlcSender(port = SerialPorts.Artnet)
    private val logger: Logger = LoggerFactory.getLogger(ArtnetHandler::class.java)

    override fun run() {
        try {
            logger.info("Starting Artnet handler ...")
            createClient()
            val networkInterface = getNetworkInterface(networkInterfaceName = getNetworkInterfaceName())
            startConnection(networkInterface = networkInterface)
            readingDmxChannels()
            logger.info("Artnet handler running")
        } catch (ex: Exception) {
            logger.error("Error occurred while running Artnet handler!\n${ex.message}")
            closeSockets()
        }
    }

    private fun readingDmxChannels() {
        logger.info("Starting Artnet reader ...")

        while (PortHandlerRunner.isRunnable()) {
            try {
                for (univers in 0..numberOfUniverses) {
                    for (subnet in 0..numberOfChannels) {
                        val data = artnetClient.readDmxData(subnet, univers)
                        sender.send(message = data[0].toInt() and 0xff)
                    }
                }
            } catch (ex: Exception) {
                logger.error("Error while reading DMX channels!\n${ex.message}")
            }
        }
    }

    private fun getNetworkInterfaceName(): String = InterfaceProperties.getArtnetInterface()

    private fun getNetworkInterface(networkInterfaceName: String): InetAddress =
            NetworkInterface.getByName(networkInterfaceName).inetAddresses.nextElement()

    private fun createClient() {
        artnetClient = ArtNetClient()
    }

    private fun startConnection(networkInterface: InetAddress) = artnetClient.start(networkInterface)

    @Synchronized
    override fun closeSockets() {
        try {
            logger.info("Closing Artnet sockets ...")

            if (::artnetClient.isInitialized) {
                artnetClient.stop()
            }

            logger.info("Artnet sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing Artnet ports!\n${ex.message}")
        }
    }
}