package net.pototskiy.apps.magemediation.database.mage.attribute

import net.pototskiy.apps.magemediation.database.VersionEntity
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeEntity
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeEntityClass
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeTable
import net.pototskiy.apps.magemediation.database.mage.MageCategories
import net.pototskiy.apps.magemediation.database.mage.MageCategory
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object MageCatBools: TypedAttributeTable<Boolean>("mage_cat_bool") {
    override val owner = reference("category", MageCategories, onDelete = ReferenceOption.CASCADE)
    override val value = bool("value")
}

class MageCatBool(id: EntityID<Int>): TypedAttributeEntity<Boolean>(id) {
    companion object: TypedAttributeEntityClass<Boolean, MageCatBool>(MageCatBools)

    override var owner: VersionEntity by MageCategory referencedOn MageCatBools.owner
    override var index by MageCatBools.index
    override var code by MageCatBools.code
    override var value by MageCatBools.value
}