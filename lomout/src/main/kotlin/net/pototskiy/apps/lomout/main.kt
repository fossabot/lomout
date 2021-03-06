package net.pototskiy.apps.lomout

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import net.pototskiy.apps.lomout.api.LOADER_LOG_NAME
import net.pototskiy.apps.lomout.api.MEDIATOR_LOG_NAME
import net.pototskiy.apps.lomout.api.PRINTER_LOG_NAME
import net.pototskiy.apps.lomout.api.ROOT_LOG_NAME
import net.pototskiy.apps.lomout.api.STATUS_LOG_NAME
import net.pototskiy.apps.lomout.api.config.ConfigurationBuilderFromDSL
import net.pototskiy.apps.lomout.api.plugable.PluginContext
import net.pototskiy.apps.lomout.database.initDatabase
import net.pototskiy.apps.lomout.loader.DataLoader
import net.pototskiy.apps.lomout.mediator.DataMediator
import net.pototskiy.apps.lomout.printer.DataPrinter
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import java.io.File

lateinit var CONFIG_BUILDER: ConfigurationBuilderFromDSL

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun main(args: Array<String>) {
    val statusLog = LogManager.getLogger(STATUS_LOG_NAME)
    val jCommander = JCommander.Builder()
        .addObject(Args)
        .build()
    try {
        @Suppress("SpreadOperator")
        jCommander.parse(*args)
    } catch (e: ParameterException) {
        jCommander.usage()
        System.exit(1)
        return
    }
    if (Args.help) {
        jCommander.usage()
        System.exit(1)
    }
    setLogLevel()

    statusLog.info("Application has started")

    CONFIG_BUILDER = ConfigurationBuilderFromDSL(
        File(Args.configFile),
        Args.scriptCacheDir,
        Args.doNotUseScriptCache
    )
    setupPluginContext()
    initDatabase(
        CONFIG_BUILDER.config.database,
        CONFIG_BUILDER.config.entityTypeManager,
        Level.toLevel(Args.sqlLogLevel)
    )
    PluginContext.logger = LogManager.getLogger(LOADER_LOG_NAME)
    CONFIG_BUILDER.config.loader?.let { DataLoader.load(CONFIG_BUILDER.config) }
    PluginContext.logger = LogManager.getLogger(MEDIATOR_LOG_NAME)
    CONFIG_BUILDER.config.mediator?.let { DataMediator.mediate(CONFIG_BUILDER.config) }
    PluginContext.logger = LogManager.getLogger(PRINTER_LOG_NAME)
    CONFIG_BUILDER.config.printer?.let { DataPrinter.print(CONFIG_BUILDER.config) }
//    MediatorFactory.create(MediatorType.CATEGORY).merge()
    statusLog.info("Application has finished")
}

fun setLogLevel() {
    Configurator.setLevel(ROOT_LOG_NAME, Level.toLevel(Args.logLevel))
}

fun setupPluginContext() {
    PluginContext.config = CONFIG_BUILDER.config
    PluginContext.entityTypeManager = CONFIG_BUILDER.config.entityTypeManager
}
