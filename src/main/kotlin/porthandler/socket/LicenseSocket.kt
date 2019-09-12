package porthandler.socket

import apibuilder.license.response.ResponseItem
import at.nlc.rsaencryptionmodule.encryption.Encryption
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.PortHandlerRunner
import porthandler.socket.interfaces.ILicenseSocket
import propertystorage.PortProperties
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class LicenseSocket: ILicenseSocket {
    private lateinit var clientSocket: Socket
    private lateinit var printWriter: PrintWriter
    private lateinit var bufferedReader: BufferedReader
    private val logger: Logger = LoggerFactory.getLogger(LicenseSocket::class.java)

    init {
        try {
            openSockets()
            receive()
        } catch (ex: Exception) {
            logger.error("Error occurred while running socket!\n${ex.message}")
            closeSockets()
        }
    }

    private fun openSockets() {
        logger.info("Opening sockets ...")
        clientSocket = Socket("127.0.0.1", PortProperties.getLicensePort())
        printWriter = PrintWriter(clientSocket.getOutputStream(), true)
        bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        logger.info("Sockets opened")
    }

    @Synchronized
    override fun send(message: JSONArray) {
        try {
            if (::printWriter.isInitialized) {
                printWriter.println(message.toString())
            } else {
                logger.error("Print writer not initialized yet!")
                openSockets()
            }
        } catch (ex: Exception) {
            logger.error("License socket failure: ${ex.message}")
            closeSockets()
            openSockets()
        }
    }

    private fun receive() {
        thread {
            bufferedReader.use {
                while (PortHandlerRunner.isRunnable()) {
                    try {
                        val inputLine = bufferedReader.readLine()

                        if (inputLine != null) {
                            val message = JSONArray(Encryption.decrypt(message = inputLine))
                            logger.info("Message '$message' received")
                            val response = ResponseItem().toObject(message = message)
                            LicenseSocketHandler.parseLicenseInformation(response = response)
                        }
                    } catch (ex: Exception) {
                        logger.error("Error occurred while parsing message!\n${ex.message}")
                    }
                }
            }
        }
    }

    private fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::printWriter.isInitialized) {
                printWriter.close()
            }

            if (::bufferedReader.isInitialized) {
                bufferedReader.close()
            }

            if (::clientSocket.isInitialized) {
                clientSocket.close()
            }

            logger.info("Sockets closed")
        } catch (ex: java.lang.Exception) {
            logger.error("Error occurred while closing sockets!\n${ex.message}")
        }
    }
}