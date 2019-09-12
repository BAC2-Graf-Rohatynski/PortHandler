package porthandler

import org.apache.log4j.BasicConfigurator

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    PortHandlerRunner.start()
}