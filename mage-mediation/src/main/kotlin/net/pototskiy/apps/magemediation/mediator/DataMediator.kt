package net.pototskiy.apps.magemediation.mediator

import net.pototskiy.apps.magemediation.api.PRINTER_LOG_NAME
import net.pototskiy.apps.magemediation.api.STATUS_LOG_NAME
import net.pototskiy.apps.magemediation.api.config.Config
import net.pototskiy.apps.magemediation.database.PipelineSets
import org.apache.logging.log4j.LogManager
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.atomic.*

object DataMediator {
    private val statusLog = LogManager.getLogger(STATUS_LOG_NAME)
    private val log = LogManager.getLogger(PRINTER_LOG_NAME)
    private var startTime = DateTime()
    private val processedRows = AtomicLong(0L)
    private const val millisInSecond: Double = 1000.0

    fun mediate(config: Config) {
        transaction { PipelineSets.deleteAll() }
        val mediator = config.mediator ?: return
        statusLog.info("Data mediating has started")
        startTime = DateTime()
        val orderedLines = mediator.lines.groupBy { it.outputEntity.name }
        orderedLines.forEach { (_, lines) ->
            lines.forEach {
                log.debug("Start creating entity<{}>", it.outputEntity.name)
                val rows = ProductionLineExecutor(config.entityTypeManager).executeLine(it)
                processedRows.addAndGet(rows)
                log.debug("Finish creating entity<{}>", it.outputEntity.name)
            }
        }
        val duration = Duration(startTime, DateTime()).millis.toDouble() / millisInSecond
        statusLog.info("Data mediating has finished, duration: ${duration}s, rows: ${processedRows.get()}")
    }
}
