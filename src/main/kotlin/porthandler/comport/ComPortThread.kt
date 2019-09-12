package porthandler.comport

import enumstorage.port.SerialPorts
import jssc.SerialPortList
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.comport.interfaces.IComPortThread
import java.lang.Exception

object ComPortThread: IComPortThread {
    private val comPorts = mutableListOf<HashMap<String, ComPortHandler>>()
    private val logger: Logger = LoggerFactory.getLogger(ComPortThread::class.java)
    private val operatingSystem = System.getProperty("os.name").toLowerCase()

    init {
        try {
            logger.info("Initializing COM port handler ...")
            initialization()
        } catch (ex: Exception) {
            logger.error("Error occurred while running COM port handler!\n${ex.message}")
        }
    }

    override fun restartAfterLicenseChange() {
        try {
            logger.info("Restarting COM ports after license change ...")
            dumpPorts()
            initialization()
        } catch (ex: Exception) {
            logger.error("Error while restarting COM ports!\n${ex.message}")
        }

        logger.info("Restarting COM ports after license change ...")
        dumpPorts()
        initialization()

    }

    private fun dumpPorts() {
        logger.info("Closing all COM ports ...")

        comPorts.forEach {
            it.values.first().closeSockets()
        }

        comPorts.clear()
        logger.info("COM ports closed")
    }

    private fun initialization() = checkLicenseAndStartPortThreads(feature = getLicenseFeature(), ports = getAllPorts())

    private fun getAllPorts(): List<String> = SerialPortList.getPortNames().toList()

    private fun isWindows(): Boolean = operatingSystem.indexOf("win") >= 0

    private fun checkLicenseAndStartPortThreads(feature: JSONObject, ports: List<String>) {
        ports.forEach { port ->
            when (port) {
                SerialPorts.COM1.name, "/dev/ttyS0", "/dev/tts/0" -> createComPort(
                        feature = feature.getString(SerialPorts.COM1.name),
                        winPort = SerialPorts.COM1,
                        unixPort = "/dev/tts/0")

                SerialPorts.COM2.name, "/dev/ttyS1", "/dev/tts/1" -> createComPort(
                        feature = feature.getString(SerialPorts.COM2.name),
                        winPort = SerialPorts.COM2,
                        unixPort = "/dev/tts/1")

                SerialPorts.COM3.name, "/dev/ttyS2", "/dev/tts/2" -> createComPort(
                        feature = feature.getString(SerialPorts.COM3.name),
                        winPort = SerialPorts.COM3,
                        unixPort = "/dev/tts/2")

                SerialPorts.COM4.name, "/dev/ttyS3", "/dev/tts/3" -> createComPort(
                        feature = feature.getString(SerialPorts.COM4.name),
                        winPort = SerialPorts.COM4,
                        unixPort = "/dev/tts/3")
            }
        }
    }

    private fun createComPort(feature: String, winPort: SerialPorts, unixPort: String) {
        if (feature == "1") {
            logger.info("${winPort.name} will be enabled ...")
            val comPort = ComPortHandler(port = if (isWindows()) winPort.name else unixPort, portName = winPort)
            comPort.start()
            val hashMap = hashMapOf<String, ComPortHandler>()
            hashMap[winPort.name] = comPort
            comPorts.add(hashMap)
        }
    }
}