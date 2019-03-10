package net.pototskiy.apps.magemediation.api.entity.writer

import net.pototskiy.apps.magemediation.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.magemediation.api.entity.DateTimeType
import net.pototskiy.apps.magemediation.api.entity.DateTimeValue
import net.pototskiy.apps.magemediation.api.entity.values.datetimeToString
import net.pototskiy.apps.magemediation.api.plugable.AttributeWriterPlugin
import net.pototskiy.apps.magemediation.api.source.workbook.Cell

open class DateTimeAttributeStringWriter : AttributeWriterPlugin<DateTimeType>() {
    var locale: String = DEFAULT_LOCALE_STR
    var pattern: String? = null

    override fun write(
        value: DateTimeType?,
        cell: Cell
    ) {
        (value as? DateTimeValue)?.let {
            cell.setCellValue(
                if (pattern != null) {
                    it.value.datetimeToString(pattern as String)
                } else {
                    it.value.datetimeToString(locale)
                }
            )
        }
    }
}
