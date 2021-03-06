package net.pototskiy.apps.lomout.api.source.workbook.csv

import net.pototskiy.apps.lomout.api.AppWorkbookException
import net.pototskiy.apps.lomout.api.CSV_SHEET_NAME
import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE
import net.pototskiy.apps.lomout.api.source.workbook.Sheet
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URL
import java.util.*
import kotlin.contracts.contract

class CsvOutputWorkbook(
    private val writer: OutputStreamWriter,
    csvFormat: CSVFormat,
    workbookLocale: Locale = DEFAULT_LOCALE
) : CsvWorkbook(workbookLocale) {

    constructor(source: URL, csvFormat: CSVFormat, workbookLocale: Locale = DEFAULT_LOCALE)
            : this(FileOutputStream(source.file).writer(), csvFormat, workbookLocale) {
        this.sourceURL = source
    }

    private var _printer: CSVPrinter = csvFormat.print(writer)

    override fun insertSheet(sheet: String): Sheet {
        if (sheet != CSV_SHEET_NAME) {
            throw AppWorkbookException("CSV workbook supports only sheet with name<default>")
        }
        return CsvSheet(this).also { this.sheet = it }
    }

    val printer: CSVPrinter
        get() = _printer

    override fun close() {
        sheet?.writeLastRow()
        _printer.close()
        writer.close()
    }
}

fun checkThatItIsCsvOutputWorkbook(workbook: CsvWorkbook) {
    contract {
        returns() implies (workbook is CsvOutputWorkbook)
    }
    if (workbook !is CsvOutputWorkbook) {
        throw AppWorkbookException("CSV workbook is not output one")
    }
}
