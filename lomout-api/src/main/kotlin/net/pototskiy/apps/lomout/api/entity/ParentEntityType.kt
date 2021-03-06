package net.pototskiy.apps.lomout.api.entity

import net.pototskiy.apps.lomout.api.AppEntityTypeException
import net.pototskiy.apps.lomout.api.PublicApi
import net.pototskiy.apps.lomout.api.config.ConfigBuildHelper
import net.pototskiy.apps.lomout.api.config.ConfigDsl

class ParentEntityType(
    val parent: EntityType,
    val include: AttributeCollection? = null,
    val exclude: AttributeCollection? = null
) {
    @ConfigDsl
    class Builder(
        private val helper: ConfigBuildHelper,
        private val parent: EntityType
    ) {
        private val includes = mutableListOf<Attribute<*>>()
        private val excludes = mutableListOf<Attribute<*>>()

        @PublicApi
        fun include(vararg name: String) {
            checkThatParentHasAttributes(parent, name.toList())
            name.toList().forEach {
                this.includes.add(helper.typeManager.getEntityAttribute(parent, it)!!)
            }
        }

        @PublicApi
        fun exclude(vararg name: String) {
            checkThatParentHasAttributes(parent, name.toList())
            name.toList().forEach {
                this.excludes.add(helper.typeManager.getEntityAttribute(parent, it)!!)
            }
        }

        fun build(): ParentEntityType = ParentEntityType(
            parent,
            if (this.includes.isEmpty()) null else AttributeCollection(this.includes),
            if (this.excludes.isEmpty()) null else AttributeCollection(this.excludes)
        )

        private fun checkThatParentHasAttributes(parent: EntityType, names: List<String>) {
            val notFound = names.minus(parent.attributes.map { it.name })
            if (notFound.isNotEmpty()) {
                throw AppEntityTypeException(
                    "Entity type<${parent.name}> has no attribute<${notFound.joinToString(",")}>"
                )
            }
        }
    }
}
