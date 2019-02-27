package net.pototskiy.apps.magemediation.api.entity.writer

import net.pototskiy.apps.magemediation.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.magemediation.api.entity.Attribute
import net.pototskiy.apps.magemediation.api.entity.BooleanType
import net.pototskiy.apps.magemediation.api.entity.BooleanValue
import net.pototskiy.apps.magemediation.api.plugable.AttributeWriterPlugin
import net.pototskiy.apps.magemediation.api.source.workbook.Cell

open class BooleanAttributeStringWriter : AttributeWriterPlugin<BooleanType>() {
    var locale: String = DEFAULT_LOCALE_STR

    override fun write(attribute: Attribute<BooleanType>, value: BooleanType?, cell: Cell) {
        (value as? BooleanValue)?.let { cell.setCellValue(it.value) }
    }
}