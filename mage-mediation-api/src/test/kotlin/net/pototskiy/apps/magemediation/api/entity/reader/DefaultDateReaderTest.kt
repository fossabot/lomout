package net.pototskiy.apps.magemediation.api.entity.reader

import net.pototskiy.apps.magemediation.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.magemediation.api.createLocale
import net.pototskiy.apps.magemediation.api.entity.Attribute
import net.pototskiy.apps.magemediation.api.entity.AttributeCollection
import net.pototskiy.apps.magemediation.api.entity.AttributeReaderWithPlugin
import net.pototskiy.apps.magemediation.api.entity.DateType
import net.pototskiy.apps.magemediation.api.entity.EntityType
import net.pototskiy.apps.magemediation.api.entity.EntityTypeManager
import net.pototskiy.apps.magemediation.api.source.workbook.Cell
import net.pototskiy.apps.magemediation.api.source.workbook.CellType
import net.pototskiy.apps.magemediation.api.source.workbook.SourceException
import net.pototskiy.apps.magemediation.api.source.workbook.Workbook
import net.pototskiy.apps.magemediation.api.source.workbook.excel.ExcelWorkbook
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFDateUtil
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.reflect.full.createInstance

@Execution(ExecutionMode.CONCURRENT)
internal class DefaultDateReaderTest {
    private val typeManager = EntityTypeManager()
    private lateinit var xlsWorkbook: HSSFWorkbook
    private lateinit var workbook: Workbook
    private lateinit var entity: EntityType
    private lateinit var attr: Attribute<DateType>
    private lateinit var xlsTestDataCell: HSSFCell
    private lateinit var inputCell: Cell

    @BeforeEach
    internal fun setUp() {
        attr = typeManager.createAttribute("attr", DateType::class)
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
        val expected = DateTimeFormat.forPattern("d.M.YY").parseDateTime("15.03.31")
        val readerEnUs = DateAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DateAttributeReader().apply { locale = "ru_RU" }
        xlsTestDataCell.setCellValue(HSSFDateUtil.getExcelDate(expected.toDate()))
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThat(readerEnUs.read(attr, inputCell)?.value).isEqualTo(expected)
        assertThat(readerRuRu.read(attr, inputCell)?.value).isEqualTo(expected)
        assertThat(inputCell.cellType).isEqualTo(CellType.DOUBLE)
        assertThat(readerEnUs.read(attr, inputCell)?.value).isEqualTo(expected)
        assertThat(readerRuRu.read(attr, inputCell)?.value).isEqualTo(expected)
    }

    @Test
    internal fun readStringCellTest() {
        val expected = DateTime().withTime(0, 0, 0, 0)
        val readerEnUs = DateAttributeReader().apply { locale = "en_US" }
        val readerRuRu = DateAttributeReader().apply { locale = "ru_RU" }
        xlsTestDataCell.setCellValue(
            expected.toString(
                DateTimeFormat.forPattern(
                    DateTimeFormat.patternForStyle("S-", "en_US".createLocale())
                )
            )
        )
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs.read(attr, inputCell)?.value).isEqualTo(expected)
        assertThatThrownBy { readerRuRu.read(attr, inputCell) }.isInstanceOf(SourceException::class.java)
        xlsTestDataCell.setCellValue(
            expected.toString(
                DateTimeFormat.forPattern(
                    DateTimeFormat.patternForStyle("S-", "ru_RU".createLocale())
                )
            )
        )
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThatThrownBy { readerEnUs.read(attr, inputCell) }.isInstanceOf(SourceException::class.java)
        assertThat(readerRuRu.read(attr, inputCell)?.value).isEqualTo(expected)
    }

    @Test
    internal fun readStringCellWithPatternTest() {
        val expected = DateTime().withTime(0, 0, 0, 0)
        val readerEnUs = DateAttributeReader().apply { pattern = "M/d/YY" }
        val readerRuRu = DateAttributeReader().apply { pattern = "d.M.YY" }
        xlsTestDataCell.setCellValue(expected.toString(DateTimeFormat.forPattern("M/d/YY")))
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThat(readerEnUs.read(attr, inputCell)?.value).isEqualTo(expected)
        assertThatThrownBy { readerRuRu.read(attr, inputCell) }.isInstanceOf(SourceException::class.java)
        xlsTestDataCell.setCellValue(expected.toString(DateTimeFormat.forPattern("d.M.YY")))
        assertThat(inputCell.cellType).isEqualTo(CellType.STRING)
        assertThatThrownBy { readerEnUs.read(attr, inputCell) }.isInstanceOf(SourceException::class.java)
        assertThat(readerRuRu.read(attr, inputCell)?.value).isEqualTo(expected)
    }

    @Test
    internal fun defaultDateReader() {
        @Suppress("UNCHECKED_CAST")
        val reader = defaultReaders[DateType::class]
        assertThat(reader).isNotNull
        assertThat(reader).isInstanceOf(AttributeReaderWithPlugin::class.java)
        reader as AttributeReaderWithPlugin
        assertThat(reader.pluginClass).isEqualTo(DateAttributeReader::class)
        val v = reader.pluginClass.createInstance() as DateAttributeReader
        @Suppress("UNCHECKED_CAST")
        v.apply(reader.options as (DateAttributeReader.() -> Unit))
        assertThat(v.locale).isEqualTo(DEFAULT_LOCALE_STR)
        assertThat(v.pattern).isEqualTo("d.M.yy")
    }
}
