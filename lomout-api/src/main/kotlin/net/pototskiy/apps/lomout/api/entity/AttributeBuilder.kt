package net.pototskiy.apps.lomout.api.entity

import net.pototskiy.apps.lomout.api.database.DbEntity
import net.pototskiy.apps.lomout.api.plugable.AttributeBuilderFunction
import net.pototskiy.apps.lomout.api.plugable.AttributeBuilderPlugin
import net.pototskiy.apps.lomout.api.plugable.PluginContext
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

sealed class AttributeBuilder<R : Type> {
    fun build(entity: DbEntity): R? {
        return when (this) {
            is AttributeBuilderWithPlugin -> pluginClass.createInstance().let {
                it.apply(options)
                it.build(entity)
            }
            is AttributeBuilderWithFunction -> PluginContext.function(entity)
        }
    }
}

class AttributeBuilderWithPlugin<R : Type>(
    val pluginClass: KClass<out AttributeBuilderPlugin<R>>,
    val options: AttributeBuilderPlugin<R>.() -> Unit = {}
) : AttributeBuilder<R>()

class AttributeBuilderWithFunction<R : Type>(
    val function: AttributeBuilderFunction<R>
) : AttributeBuilder<R>()
