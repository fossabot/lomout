package net.pototskiy.apps.lomout.printer

import net.pototskiy.apps.lomout.api.PRINTER_LOG_NAME
import net.pototskiy.apps.lomout.api.config.mediator.AbstractLine
import net.pototskiy.apps.lomout.api.config.printer.PrinterLine
import net.pototskiy.apps.lomout.api.entity.AnyTypeAttribute
import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import net.pototskiy.apps.lomout.api.entity.Type
import net.pototskiy.apps.lomout.mediator.LineExecutor
import net.pototskiy.apps.lomout.mediator.PipelineExecutor
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PrinterLineExecutor(entityTypeManager: EntityTypeManager) :
    LineExecutor(
        entityTypeManager,
        System.getenv("printer.line.cache.size")?.toIntOrNull() ?: 0
    ) {

    override val logger: Logger = LogManager.getLogger(PRINTER_LOG_NAME)
    private lateinit var printer: EntityPrinter

    override fun processResultData(data: Map<AnyTypeAttribute, Type?>): Long =
        if (data.isEmpty()) {
            0L
        } else {
            printer.print(data)
        }

    override fun preparePipelineExecutor(line: AbstractLine): PipelineExecutor = PipelineExecutor(
        entityTypeManager,
        line.pipeline,
        line.inputEntities,
        line.inputEntities.first().entity,
        pipelineDataCache
    )

    @Suppress("TooGenericExceptionCaught", "SpreadOperator")
    override fun executeLine(line: AbstractLine): Long {
        line as PrinterLine
        val entityPrinter = EntityPrinter(
            line.outputFieldSets.file,
            line.outputFieldSets.fieldSets,
            line.outputFieldSets.printHead
        )
        entityPrinter.use {
            this.printer = it
            super.executeLine(line)
        }
        return processedRows
    }
}
