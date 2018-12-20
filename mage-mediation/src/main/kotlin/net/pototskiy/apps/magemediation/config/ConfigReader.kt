package net.pototskiy.apps.magemediation.config

import net.pototskiy.apps.magemediation.Args
import net.pototskiy.apps.magemediation.LOG_NAME
import org.slf4j.LoggerFactory
import java.io.File
import javax.xml.bind.JAXBContext

fun readConfig(): Config? {
    val logger = LoggerFactory.getLogger(LOG_NAME)
    val jaxbContext = JAXBContext.newInstance(Config::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    unmarshaller.listener = UnmarshalListener()
    var config: Config? = null
    try {
        File(Args.configFile).reader().use {
            config = unmarshaller.unmarshal(it) as Config
        }
    } catch (e: ConfigException) {
        logger.error("Configuration error: ${e.message}")
        System.exit(1)
    }
    return config
}