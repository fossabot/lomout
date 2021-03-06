package net.pototskiy.apps.lomout.api.source.workbook.csv

import net.pototskiy.apps.lomout.api.AppSheetException
import net.pototskiy.apps.lomout.api.AppWorkbookException
import net.pototskiy.apps.lomout.api.CSV_SHEET_NAME
import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE
import net.pototskiy.apps.lomout.api.createLocale
import net.pototskiy.apps.lomout.api.entity.values.datetimeToString
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.WorkbookFactory
import net.pototskiy.apps.lomout.api.source.workbook.WorkbookType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.joda.time.DateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.File
import java.util.*

@Suppress("MagicNumber")
@Execution(ExecutionMode.CONCURRENT)
internal class CsvWorkbookTest {
    private lateinit var file: File

    @BeforeEach
    internal fun setUp() {
        val baseFile = File("${System.getenv("TEST_DATA_DIR")}/csv-workbook-test.csv")
        file = File("../tmp/${UUID.randomUUID()}.csv")
        baseFile.copyTo(file)
    }

    @AfterEach
    internal fun tearDown() {
        file.delete()
    }

    @Test
    internal fun workbookBasicTest() {
        WorkbookFactory.create(file.toURI().toURL(), "en_US".createLocale()).use { workbook ->
            assertThat(workbook).isInstanceOf(CsvInputWorkbook::class.java)
            assertThat(workbook.type).isEqualTo(WorkbookType.CSV)
            assertThat(workbook.name).isEqualTo(file.name)
            assertThat(workbook.hasSheet("Sheet1")).isFalse()
            assertThat(workbook.hasSheet(CSV_SHEET_NAME)).isTrue()
            assertThat(workbook[CSV_SHEET_NAME]).isNotNull.isInstanceOf(CsvSheet::class.java)
            assertThat(workbook[CSV_SHEET_NAME].name).isEqualTo(workbook[0].name)
            assertThatThrownBy { workbook["Sheet1"] }.isInstanceOf(AppWorkbookException::class.java)
            assertThatThrownBy { workbook[1] }.isInstanceOf(AppWorkbookException::class.java)
            assertThatThrownBy { workbook.insertSheet("test") }
                .isInstanceOf(AppWorkbookException::class.java)
        }
        val tmpFile = File.createTempFile("csv-test", ".csv", File("../tmp/"))
        try {
            WorkbookFactory.create(tmpFile.toURI().toURL(), "en_US".createLocale(), false).use { workbook ->
                assertThat(workbook.insertSheet(CSV_SHEET_NAME)).isNotNull
                assertThatThrownBy { workbook.insertSheet("test") }
                    .isInstanceOf(AppWorkbookException::class.java)
            }
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    internal fun sheetBasicTest() {
        WorkbookFactory.create(file.toURI().toURL(), "en_US".createLocale()).use { workbook ->
            val sheet = workbook[CSV_SHEET_NAME]
            assertThat(sheet.workbook.name).isEqualTo(file.name)
            assertThat(sheet.name).isEqualTo(CSV_SHEET_NAME)
            assertThat(sheet[0]).isNotNull.isInstanceOf(CsvRow::class.java)
            assertThatThrownBy { sheet[2] }.isInstanceOf(AppSheetException::class.java)
            assertThatThrownBy { sheet.insertRow(1) }.isInstanceOf(AppWorkbookException::class.java)
        }
        val tmpFile = File.createTempFile("csv-test", ".csv", File("../tmp/"))
        try {
            WorkbookFactory.create(tmpFile.toURI().toURL(), "en_US".createLocale(), false).use { workbook ->
                val sheet = workbook.insertSheet(CSV_SHEET_NAME)
                val row = sheet.insertRow(0)
                assertThat(row).isNotNull.isInstanceOf(CsvRow::class.java)
            }
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    internal fun rowBasicTest() {
        WorkbookFactory.create(file.toURI().toURL(), "en_US".createLocale()).use { workbook ->
            val sheet = workbook[CSV_SHEET_NAME]
            val row = sheet[0]
            assertThat(row).isNotNull.isInstanceOf(CsvRow::class.java)
            assertThat(row!!.sheet.name).isEqualTo(CSV_SHEET_NAME)
            assertThat(row.countCell()).isEqualTo(4)
            assertThat(row[0]).isNotNull.isInstanceOf(CsvCell::class.java)
            assertThat(row[5]).isNull()
            assertThat(row.getOrEmptyCell(1)).isNotNull.isInstanceOf(CsvCell::class.java)
            assertThat(row.getOrEmptyCell(5)).isNotNull.isInstanceOf(CsvCell::class.java)
            assertThatThrownBy { row.insertCell(5) }.isInstanceOf(AppWorkbookException::class.java)
        }
        val tmpFile = File.createTempFile("csv-test", ".csv", File("../tmp/"))
        try {
            WorkbookFactory.create(tmpFile.toURI().toURL(), "en_US".createLocale(), false).use { workbook ->
                val sheet = workbook.insertSheet(CSV_SHEET_NAME)
                val row = sheet.insertRow(0)
                assertThat(row.insertCell(0)).isNotNull.isInstanceOf(CsvCell::class.java)
            }
        } finally {
            tmpFile.delete()
        }
    }

    @Test
    internal fun cellBasicTest() {
        WorkbookFactory.create(file.toURI().toURL(), "en_US".createLocale()).use { workbook ->
            val sheet = workbook[CSV_SHEET_NAME]
            val row = sheet[0]!!
            assertThat(row[0]!!.cellType).isEqualTo(CellType.LONG)
            assertThat(row[0]!!.longValue).isEqualTo(11L)
            assertThat(row[1]!!.cellType).isEqualTo(CellType.DOUBLE)
            assertThat(row[1]!!.doubleValue).isEqualTo(22.33)
            assertThat(row[2]!!.cellType).isEqualTo(CellType.BOOL)
            assertThat(row[2]!!.booleanValue).isEqualTo(true)
            assertThat(row[3]!!.cellType).isEqualTo(CellType.STRING)
            assertThat(row[3]!!.stringValue).isEqualTo("just text")
            assertThat(row[0]!!.row.rowNum).isEqualTo(row.rowNum)
            Unit
        }
        val tmpFile = File.createTempFile("csv-test", ".csv", File("../tmp/"))
        try {
            WorkbookFactory.create(tmpFile.toURI().toURL(), "en_US".createLocale(), false).use { workbook ->
                val sheet = workbook.insertSheet(CSV_SHEET_NAME)
                val row = sheet.insertRow(0)
                val cell = row.insertCell(0)
                val now = DateTime.now()
                assertThat(cell.cellType).isEqualTo(CellType.BLANK)
                cell.setCellValue("test")
                assertThat(cell.cellType).isEqualTo(CellType.STRING)
                assertThat(cell.stringValue).isEqualTo("test")
                cell.setCellValue(true)
                assertThat(cell.cellType).isEqualTo(CellType.LONG)
                assertThat(cell.longValue).isEqualTo(1)
                cell.setCellValue(22.33)
                assertThat(cell.cellType).isEqualTo(CellType.DOUBLE)
                assertThat(cell.doubleValue).isEqualTo(22.33)
                cell.setCellValue(11L)
                assertThat(cell.cellType).isEqualTo(CellType.LONG)
                assertThat(cell.longValue).isEqualTo(11L)
                cell.setCellValue(now)
                assertThat(cell.cellType).isEqualTo(CellType.STRING)
                assertThat(cell.stringValue).isEqualTo(now.datetimeToString(DEFAULT_LOCALE))
                Unit
            }
        } finally {
            tmpFile.delete()
        }
    }
}
