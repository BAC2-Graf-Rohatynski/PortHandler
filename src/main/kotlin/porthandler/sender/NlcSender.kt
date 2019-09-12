package porthandler.sender

import enumstorage.port.SerialPorts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.sender.interfaces.INlcSender
import propertystorage.PortProperties
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class NlcSender (private val port: SerialPorts): INlcSender {
    private lateinit var clientSocket: Socket
    private lateinit var printWriter: PrintWriter
    private lateinit var bufferedReader: BufferedReader
    private val logger: Logger = LoggerFactory.getLogger(NlcSender::class.java)

    init {
        try {
            openSockets()
        } catch (ex: Exception) {
            logger.error("Error occurred while running NLC sender\n${ex.message}")
            closeSockets()
        }
    }

    @Synchronized
    override fun send(message: ByteArray) {
        try {
            if (::printWriter.isInitialized) {
                printWriter.println(message)
            } else {
                logger.error("Print writer not initialized yet!")
                openSockets()
            }
        } catch (ex: Exception) {
            logger.error("Sender socket failure: ${ex.message}")
            closeSockets()
            openSockets()
        }
    }

    @Synchronized
    override fun send(message: Int) = send(message = ByteArray(message))

    private fun openSockets() {
        logger.info("Starting ${port.name} port sender socket ...")
        clientSocket = Socket("127.0.0.1", PortProperties.getPipelinePort())
        printWriter = PrintWriter(clientSocket.getOutputStream(), true)
        bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        logger.info("${port.name} port socket established ...")
    }

    private fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::printWriter.isInitialized) {
                printWriter.close()
            }

            if (::clientSocket.isInitialized) {
                clientSocket.close()
            }

            logger.info("Sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing NLC sockets\n${ex.message}")
        }
    }
}