package net.pototskiy.apps.magemediation.loader.converter

import net.pototskiy.apps.magemediation.config.excel.DateDefinition
import net.pototskiy.apps.magemediation.config.excel.Field
import net.pototskiy.apps.magemediation.config.excel.ListDefinition
import net.pototskiy.apps.magemediation.loader.LoaderException
import net.pototskiy.apps.magemediation.source.Cell
import net.pototskiy.apps.magemediation.source.CellType
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*

class DateConverter(
    private val cell: Cell,
    private val field: Field
) {
    fun convert(): DateTime = when (cell.cellType) {
        CellType.INT -> DateTime(Date(cell.intValue))
        CellType.DOUBLE -> DateTime(Date(cell.doubleValue.toLong()))
        CellType.BOOL -> throw LoaderException("Field<${field.name}, boolean can not be converted to date")
        CellType.STRING -> stringToDate(cell.stringValue)
    }

    fun convertList(): List<DateTime> {
        return when (cell.cellType) {
            CellType.INT -> listOf(DateTime(Date(cell.intValue)))
            CellType.DOUBLE -> listOf(DateTime(Date(cell.doubleValue.toLong())))
            CellType.BOOL -> throw LoaderException("Field<${field.name}, boolean can not be converted to date")
            CellType.STRING -> ValueListParser(
                cell.stringValue,
                field.typeDefinitions.findLast { it is ListDefinition } as ListDefinition
            )
                .parse()
                .map { stringToDate(it) }
        }
    }

    private fun stringToDate(value: String): DateTime {
        val format = (field.typeDefinitions.findLast { it is DateDefinition } as DateDefinition).format
        return try {
            DateTimeFormat.forPattern(format).parseDateTime(value)
        } catch (e: IllegalArgumentException) {
            throw LoaderException("Field<${field.name}>, string can not be converted to date with pattern $format")
        } catch (e: UnsupportedOperationException) {
            throw LoaderException("Field<${field.name}>, string can not be converted to date with pattern $format")
        }
    }
}