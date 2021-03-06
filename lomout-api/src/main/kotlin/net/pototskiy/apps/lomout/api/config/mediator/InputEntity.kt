package net.pototskiy.apps.lomout.api.config.mediator

import net.pototskiy.apps.lomout.api.AppAttributeException
import net.pototskiy.apps.lomout.api.config.ConfigBuildHelper
import net.pototskiy.apps.lomout.api.config.ConfigDsl
import net.pototskiy.apps.lomout.api.database.DbEntity
import net.pototskiy.apps.lomout.api.database.DbEntityTable
import net.pototskiy.apps.lomout.api.entity.AnyTypeAttribute
import net.pototskiy.apps.lomout.api.entity.Attribute
import net.pototskiy.apps.lomout.api.entity.AttributeAsCell
import net.pototskiy.apps.lomout.api.entity.AttributeCollection
import net.pototskiy.apps.lomout.api.entity.AttributeReader
import net.pototskiy.apps.lomout.api.entity.EntityType
import net.pototskiy.apps.lomout.api.entity.Type
import net.pototskiy.apps.lomout.api.plugable.SqlFilterPlugin
import org.jetbrains.exposed.sql.Alias
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import java.util.*
import kotlin.collections.set

data class InputEntity(
    val entity: EntityType,
    val entityExtension: EntityType?,
    val filter: SqlFilter?,
    val extAttrMaps: AttrMapCollection
) {
    fun extendedAttributes(entity: DbEntity): Map<AnyTypeAttribute, Type?> {
        return extAttrMaps.keys.map { attr ->
            @Suppress("UNCHECKED_CAST")
            attr to (attr.reader as AttributeReader<Type>)
                .read(attr, AttributeAsCell(extAttrMaps[attr] as Attribute<Type>, entity.data[extAttrMaps[attr]]))
        }.toMap()
    }

    @ConfigDsl
    class Builder(
        val helper: ConfigBuildHelper,
        val entityType: EntityType
    ) {
        val attrPairs = mutableMapOf<Attribute<*>, Attribute<*>>()
        var sqlFilter: SqlFilter? = null
        private val extEntityUUID = UUID.randomUUID().toString()

        @ConfigDsl
        fun filter(block: SqlExpressionBuilder.(alias: Alias<DbEntityTable>) -> Op<Boolean>) {
            sqlFilter = SqlFilterWithFunction { alias: Alias<DbEntityTable> -> Op.build { block(alias) } }
        }

        @ConfigDsl
        inline fun <reified P : SqlFilterPlugin> filter(noinline block: P.() -> Unit = {}) {
            @Suppress("UNCHECKED_CAST")
            sqlFilter = SqlFilterWithPlugin(P::class, block as (SqlFilterPlugin.() -> Unit))
        }

        @ConfigDsl
        inline fun <reified T : Type> extAttribute(
            name: String,
            from: String,
            block: Attribute.Builder<T>.() -> Unit = {}
        ) {
            val destAttr = Attribute.Builder(helper, name, T::class).apply(block).build()
            val origData = this.helper.typeManager.getEntityAttribute(entityType, from)
                ?: throw AppAttributeException("Attribute<${entityType.name}:$from> is not defined>")
            attrPairs[origData] = destAttr
        }

        private fun extendedName(type: String): String = "$type${"$$"}ext${"$$"}$extEntityUUID"

        fun build(): InputEntity {
            val extEntity = if (attrPairs.isEmpty()) {
                null
            } else {
                helper.typeManager.createEntityType(
                    extendedName(entityType.name),
                    emptyList(),
                    false
                ).also { helper.typeManager.initialAttributeSetup(it, AttributeCollection(attrPairs.values.toList())) }
            }
            return InputEntity(
                entityType,
                extEntity,
                sqlFilter,
                AttrMapCollection(attrPairs.map { it.value to it.key }.toMap())
            )
        }
    }
}
