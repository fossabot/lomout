package net.pototskiy.apps.magemediation.loader

import net.pototskiy.apps.magemediation.config.excel.Dataset
import net.pototskiy.apps.magemediation.config.excel.Field
import net.pototskiy.apps.magemediation.config.excel.Field.Companion.UNDEFINED_COLUMN
import net.pototskiy.apps.magemediation.config.excel.FieldSetType
import net.pototskiy.apps.magemediation.config.excel.FieldType
import net.pototskiy.apps.magemediation.source.Sheet

class Headers(private val sheet: Sheet, private val dataset: Dataset) {

    fun getHeaders(): List<Field> {
        val confHeaders = dataset.fieldSets.find { it.type == FieldSetType.MAIN }?.fields
            ?: listOf()
        return if (dataset.headersRow != -1) {
            val sourceHeaders = mutableListOf<Field>()
            sheet[dataset.headersRow].forEachIndexed { c, cell ->
                sourceHeaders.add(Field().apply {
                    name = cell.stringValue
                    column = c
                    type = FieldType.STRING
                })
            }
            val needUpdate = sourceHeaders.filter { field -> confHeaders.any { it.name == field.name } }
            sourceHeaders.removeIf { field -> confHeaders.any { it.name == field.name } }
            confHeaders.forEach { field ->
                if (field.nested) {
                    sourceHeaders.add(field)
                } else {
                    sourceHeaders.add(
                        Field().apply {
                            name = field.name
                            column = if (field.column == UNDEFINED_COLUMN)
                                needUpdate.find { it.name == field.name }?.column as Int
                            else
                                field.column
                            regex = field.regex
                            type = field.type
                            typeDefinitions = field.typeDefinitions
                            keyField = field.keyField
                            optional = field.optional
                            locale = field.locale
                        })
                }
            }
            sourceHeaders.toList()
        } else {
            confHeaders.toList()
        }
    }
}