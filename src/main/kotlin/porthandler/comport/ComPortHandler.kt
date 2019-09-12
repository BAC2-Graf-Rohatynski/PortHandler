package porthandler.comport

import enumstorage.port.SerialPorts
import jssc.SerialPort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.PortHandlerRunner
import porthandler.comport.interfaces.IComPortHandler
import porthandler.sender.NlcSender
import java.lang.Exception

class ComPortHandler(private val port: String, val portName: SerialPorts): IComPortHandler, Thread() {
    private lateinit var serialPort: SerialPort
    private val comPortSender = NlcSender(port = portName)
    private val logger: Logger = LoggerFactory.getLogger(ComPortHandler::class.java)

    override fun run() {
        try {
            createComPort()
            openComPort()
            setParameters()
            readingPort()
        } catch (ex: Exception) {
            logger.error("Error occurred while running COM port '$port' handler!\n${ex.message}")
            closeSockets()
        }
    }

    private fun createComPort() {
        logger.info("Creating COM port '$port' ...")
        serialPort = SerialPort(port)
    }

    private fun openComPort() {
        logger.info("Opening COM port '$port' ...")
        serialPort.openPort()
    }

    private fun setParameters() {
        logger.info("Setting parameters for COM port '$port' ...")
        serialPort.setParams(
                SerialPort.BAUDRATE_9600,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE)
    }

    private fun readingPort() {
        logger.info("Starting read thread for COM port '$port' ...")

        while (PortHandlerRunner.isRunnable()) {
            try {
                //Read 5 bytes from serial port
                val buffer = serialPort.readBytes(5)
                comPortSender.send(message = buffer)
            } catch (ex: Exception) {
                logger.error("Error while reading COM port!\n${ex.message}")
            }
        }
    }

    override fun closeSockets() {
       try {
           logger.info("Closing COM port '$port' ...")

           if (::serialPort.isInitialized) {
               serialPort.closePort()
           }

           logger.info("COM port '$port' closed")
       } catch (ex: Exception) {
           logger.error("Error occurred while closing COM port '$port'!\n${ex.message}")
       }
    }
}