package net.pototskiy.apps.lomout.api.entity.reader

import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.lomout.api.entity.Attribute
import net.pototskiy.apps.lomout.api.entity.AttributeCollection
import net.pototskiy.apps.lomout.api.entity.AttributeReaderWithPlugin
import net.pototskiy.apps.lomout.api.entity.DoubleType
import net.pototskiy.apps.lomout.api.entity.EntityType
import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.csv.CsvCell
import net.pototskiy.apps.lomout.api.source.workbook.csv.CsvInputWorkbook
import net.pototskiy.apps.lomout.api.source.workbook.excel.ExcelWorkbook
import org.apache.commons.csv.CSVFormat
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.text.ParseException
import kotlin.reflect.full.createInstance

@Suppress("MagicNumber")
@Execution(ExecutionMode.CONCURRENT)
internal class DefaultDoubleReaderTest {
    private val typeManager = EntityTypeManager()
    private lateinit var xlsWorkbook: HSSFWorkbook
    private lateinit var workbook: Workbook
    private lateinit var entity: EntityType
    private lateinit var attr: Attribute<DoubleType>
    private lateinit var xlsTestDataCell: HSSFCell
    private lateinit var inputCell: Cell

    @BeforeEach
    internal fun setUp() {
        attr = typeManager.createAttribute("attr", DoubleType::class)
        entity = typeManager.createEntityType("test", emptyList(), false).also {
            typeManager.initialAttributeSetup(it, AttributeCollection(listOf(attr)))
        }
        xlsWorkbook = HSSFWorkbookFactory.createWorkbook()
        val xlsSheet = xlsWorkbook.createSheet("test-data")
        xlsSheet.isActive = true
        xlsTestDataCell = xlsSheet.createRow(0).createCell(0)
        workbook = ExcelWorkbook(xlsWorkbook)
        inputCell = workbook["test-data"][0]!![0]!!
    }

    @AfterEach
    internal fun tearDown() {
        workbook.close()
    }

    @Test
    internal fun readDoubleCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setCellValue(1.1)
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThat(reader.read(attr, inputCell)?.value).isEqualTo(1.1)
    }

    @Test
    internal fun readLongCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        val cell = createCsvCell("11")
        assertThat(cell.cellType).isEqualTo(CellType.LONG)
        assertThat(reader.read(attr, cell)?.value).isEqualTo(11.0)
    }

    @Test
    internal fun readBooleanCellTest() {
        val reader = DoubleAttributeReader().apply { locale = "en_US" }
        xlsTestDataCell.setCellValue(true)
        assertThat(inputCell.cellType).isEqualTo(CellType.BOOL)
        assertThat(reader.read(attr, inputCell)?.value).isEqualTo(1.0)
        xlsTestDataCell.setCellValue(false)
        assertThat(inputCell.cellType).isEqualTo(CellType.BOOL)
        assertThat(reader.read(attr, inputCell)?.value).isEqualTo(0.0)
    }

    @Test
    internal fun readStringEnUsCellTest() {
        val readerEnUs = DoubleAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DoubleAttributeReader().apply { locale = "ru_RU" }
        xlsTestDataCell.setCellValue("1.1")
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs.read(attr, inputCell)?.value).isEqualTo(1.1)
        assertThatThrownBy { readerRuRu.read(attr, inputCell) }.isInstanceOf(ParseException::class.java)
    }

    @Test
    internal fun defaultDoubleReaderTest() {
        @Suppress("UNCHECKED_CAST")
        val reader = defaultReaders[DoubleType::class]
        assertThat(reader).isNotNull
        assertThat(reader).isInstanceOf(AttributeReaderWithPlugin::class.java)
        reader as AttributeReaderWithPlugin
        assertThat(reader.pluginClass).isEqualTo(DoubleAttributeReader::class)
        val v = reader.pluginClass.createInstance() as DoubleAttributeReader
        @Suppress("UNCHECKED_CAST")
        v.apply(reader.options as (DoubleAttributeReader.() -> Unit))
        assertThat(v.locale).isEqualTo(DEFAULT_LOCALE_STR)
    }

    private fun createCsvCell(value: String): CsvCell {
        val reader = value.byteInputStream().reader()
        CsvInputWorkbook(reader, CSVFormat.RFC4180).use {
            return it[0][0][0]!!
        }
    }
}
