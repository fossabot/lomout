package net.pototskiy.apps.magemediation.api.entity.writer

import net.pototskiy.apps.magemediation.api.entity.Attribute
import net.pototskiy.apps.magemediation.api.entity.TextType
import net.pototskiy.apps.magemediation.api.entity.TextValue
import net.pototskiy.apps.magemediation.api.plugable.AttributeWriterPlugin
import net.pototskiy.apps.magemediation.api.source.workbook.Cell

open class TextAttributeStringWriter : AttributeWriterPlugin<TextType>() {
    override fun write(attribute: Attribute<TextType>, value: TextType?, cell: Cell) {
        (value as? TextValue)?.let { cell.setCellValue(it.value) }
    }
}