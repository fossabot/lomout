package net.pototskiy.apps.lomout.api.config.loader

import net.pototskiy.apps.lomout.api.AppConfigException
import net.pototskiy.apps.lomout.api.config.Config
import net.pototskiy.apps.lomout.api.config.ConfigBuildHelper
import net.pototskiy.apps.lomout.api.entity.AttributeListType
import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import net.pototskiy.apps.lomout.api.entity.LongType
import net.pototskiy.apps.lomout.api.entity.StringType
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
internal class FieldSetBuilderTest {
    private val typeManager = EntityTypeManager()
    private val helper = ConfigBuildHelper(typeManager)
    private val entity = typeManager.createEntityType("test", emptyList(), true)

    @Test
    internal fun noFieldDefinedTest() {
        val fs = FieldSet.Builder(
            helper,
            entity,
            "test",
            true,
            false,
            null,
            null
        ).apply {
        }
        Assertions.assertThatThrownBy { fs.build() }.isInstanceOf(AppConfigException::class.java)
    }

    @Test
    internal fun uniqueFieldNameTest() {
        assertThatThrownBy {
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1")
                field("f1")
            }.build()
        }.isInstanceOf(AppConfigException::class.java)
    }

    @Test
    internal fun uniqueFieldColumnTest() {
        assertThatThrownBy {
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1") { column(0) }
                field("f2") { column(0) }
            }.build()
        }.isInstanceOf(AppConfigException::class.java)
    }

    @Test
    internal fun assignUndefinedAttributeTest() {
        assertThatThrownBy {
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1") to attribute("f1")
            }.build()
        }.isInstanceOf(AppConfigException::class.java)
    }

    @Test
    internal fun assignAttributeWithNullNameTest() {
        assertThatThrownBy {
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1") to attribute<StringType>(null) {}
            }.build()
        }.isInstanceOf(AppConfigException::class.java)
    }

    @Test
    internal fun assignAttributeWithDefinitionTest() {
        assertThat(
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1") to attribute<StringType>("f2") {}
            }.build()
        ).isNotNull
    }

    @Test
    internal fun wrongParentTest() {
        assertThatThrownBy {
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1")
                field("f2") { parent("f3") }
            }.build()
        }.isInstanceOf(AppConfigException::class.java)
    }

    @Test
    internal fun cycleNestedParentTest() {
        assertThat(
            FieldSet.Builder(
                helper,
                entity,
                "test",
                true,
                false,
                null,
                null
            ).apply {
                field("f1") { } to attribute<AttributeListType>("f1") {}
                field("f2") { parent("f1") } to attribute<AttributeListType>("f2") {}
                field("f3") { parent("f2") } to attribute<AttributeListType>("f3") {}
            }.build()
        ).isNotNull
    }

    @Test
    internal fun createFieldSetWithoutMainTest() {
        assertThatThrownBy { createConfWithoutMainSet()}
            .isInstanceOf(AppConfigException::class.java)
            .hasMessageContaining("Field set collection must contain main set")
    }

    private fun createConfWithoutMainSet(): Config {
        return Config.Builder(helper).apply {
            database {
                server {
                    host("localhost")
                    port(3306)
                    user("root")
                    if (System.getenv("TRAVIS_BUILD_DIR") == null) {
                        password("root")
                    } else {
                        password("")
                    }
                }
            }
            loader {
                files {
                    val testDataDir = System.getenv("TEST_DATA_DIR")
                    file("test-data") { path("$testDataDir/entity-loader-add-test.csv") }
                }
                entities {
                    entity("entity", false) {
                        attribute<LongType>("key") { key() }
                        attribute<StringType>("data")
                    }
                }
                loadEntity("entity") {
                    fromSources { source { file("test-data"); sheet("default"); stopOnEmptyRow() } }
                    rowsToSkip(1)
                    keepAbsentForDays(1)
                    sourceFields {
                        extra("entity") {
                            field("key") { column(0) }
                            field("data") { column(1) }
                        }
                    }
                }
            }
            mediator {
                crossProductionLine {
                    output("output") {
                        inheritFrom("entity")
                    }
                    input {
                        entity("entity")
                    }
                    pipeline {
                        assembler { _, _ -> emptyMap() }
                    }
                }
            }
        }.build()
    }

}
