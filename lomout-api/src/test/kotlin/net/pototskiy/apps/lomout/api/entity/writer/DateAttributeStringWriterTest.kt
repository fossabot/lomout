package net.pototskiy.apps.lomout.api.entity.writer

import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE
import net.pototskiy.apps.lomout.api.DEFAULT_LOCALE_STR
import net.pototskiy.apps.lomout.api.entity.AttributeWriter
import net.pototskiy.apps.lomout.api.entity.AttributeWriterWithPlugin
import net.pototskiy.apps.lomout.api.entity.DateType
import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import net.pototskiy.apps.lomout.api.entity.values.dateToString
import net.pototskiy.apps.lomout.api.source.workbook.Cell
import net.pototskiy.apps.lomout.api.source.workbook.CellType
import net.pototskiy.apps.lomout.api.source.workbook.Workbook
import net.pototskiy.apps.lomout.api.source.workbook.WorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.io.File
import java.util.*
import kotlin.reflect.full.createInstance

@Suppress("MagicNumber")
@Execution(ExecutionMode.CONCURRENT)
internal class DateAttributeStringWriterTest {
    private lateinit var typeManager: EntityTypeManager
    private lateinit var file: File
    private lateinit var workbook: Workbook
    private lateinit var cell: Cell

    @BeforeEach
    internal fun setUp() {
        typeManager = EntityTypeManager()
        file = File("../tmp/${UUID.randomUUID()}.xls")
        workbook = WorkbookFactory.create(file.toURI().toURL(), DEFAULT_LOCALE, false)
        cell = workbook.insertSheet("test").insertRow(0).insertCell(0)
    }

    @AfterEach
    internal fun tearDown() {
        workbook.close()
        file.delete()
    }

    @Test
    internal fun simpleWriteTest() {
        val attr = typeManager.createAttribute("attr", DateType::class)
        val now = DateTime.now()
        val value = DateType(now)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
        @Suppress("UNCHECKED_CAST")
        (attr.writer as AttributeWriter<DateType>).write(value, cell)
        assertThat(cell.cellType).isEqualTo(CellType.STRING)
        assertThat(cell.stringValue)
            .isEqualTo(now.toString(DateTimeFormat.forPattern("d.M.yy")))
    }

    @Test
    internal fun simpleWriteLocaleTest() {
        val attr = typeManager.createAttribute("attr", DateType::class) {
            writer(AttributeWriterWithPlugin(
                DateAttributeStringWriter::class
            ) {
                this as DateAttributeStringWriter
                locale = DEFAULT_LOCALE_STR
                pattern = null
            })
        }
        val now = DateTime.now()
        val value = DateType(now)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
        @Suppress("UNCHECKED_CAST")
        (attr.writer as AttributeWriter<DateType>).write(value, cell)
        assertThat(cell.cellType).isEqualTo(CellType.STRING)
        assertThat(cell.stringValue)
            .isEqualTo(
                now.dateToString(DEFAULT_LOCALE)
            )
    }

    @Test
    internal fun writeNullValueTest() {
        val attr = typeManager.createAttribute("attr", DateType::class)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
        @Suppress("UNCHECKED_CAST")
        (attr.writer as AttributeWriter<DateType>).write(null, cell)
        assertThat(cell.cellType).isEqualTo(CellType.BLANK)
    }

    @Test
    internal fun defaultWriterTest() {
        val writer = defaultWriters[DateType::class]
        assertThat(writer).isNotNull
        assertThat(writer).isInstanceOf(AttributeWriterWithPlugin::class.java)
        writer as AttributeWriterWithPlugin
        assertThat(writer.pluginClass).isEqualTo(DateAttributeStringWriter::class)
        val v = writer.pluginClass.createInstance() as DateAttributeStringWriter
        @Suppress("UNCHECKED_CAST")
        v.apply(writer.options as (DateAttributeStringWriter.() -> Unit))
        assertThat(v.locale).isEqualTo(DEFAULT_LOCALE_STR)
        assertThat(v.pattern).isEqualTo("d.M.yy")
    }
}
