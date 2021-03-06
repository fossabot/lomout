package net.pototskiy.apps.lomout.api.source.workbook

import java.io.Closeable

interface Workbook : Iterable<Sheet>, Closeable {
    val name: String
    val type: WorkbookType
    operator fun get(sheet: String): Sheet
    operator fun get(sheet: Int): Sheet
    fun insertSheet(sheet: String): Sheet
    fun hasSheet(sheet: String): Boolean
}
