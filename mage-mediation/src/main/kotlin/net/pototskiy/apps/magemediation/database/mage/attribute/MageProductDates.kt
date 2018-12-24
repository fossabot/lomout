package net.pototskiy.apps.magemediation.database.mage.attribute

import net.pototskiy.apps.magemediation.database.VersionEntity
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeTable
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeEntity
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeEntityClass
import net.pototskiy.apps.magemediation.database.mage.MageProduct
import net.pototskiy.apps.magemediation.database.mage.MageProducts
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.joda.time.DateTime

object MageProductDates : TypedAttributeTable<DateTime>("mage_product_date") {
    override val owner = reference("product", MageProducts, onDelete = ReferenceOption.CASCADE)
    override val value = date("value")
}

class MageProductDate(id: EntityID<Int>) : TypedAttributeEntity<DateTime>(id) {
    companion object : TypedAttributeEntityClass<DateTime, MageProductDate>(MageProductDates)

    override var owner: VersionEntity by MageProduct referencedOn MageProductDates.owner
    override var index by MageProductDates.index
    override var code by MageProductDates.code
    override var value by MageProductDates.value
}
