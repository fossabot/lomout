package net.pototskiy.apps.magemediation.database.onec.attribute

import net.pototskiy.apps.magemediation.database.VersionEntity
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeTable
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeEntity
import net.pototskiy.apps.magemediation.database.attribute.TypedAttributeEntityClass
import net.pototskiy.apps.magemediation.database.onec.OnecProduct
import net.pototskiy.apps.magemediation.database.onec.OnecProducts
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object OnecProductTexts : TypedAttributeTable<String>("onec_product_text") {
    override val product = reference("product", OnecProducts, onDelete = ReferenceOption.CASCADE)
    override val value = text("value")
}

class OnecProductText(id: EntityID<Int>) : TypedAttributeEntity<String>(id) {
    companion object : TypedAttributeEntityClass<String, OnecProductText>(OnecProductTexts)

    override var product: VersionEntity by OnecProduct referencedOn OnecProductTexts.product
    override var index by OnecProductTexts.index
    override var code by OnecProductTexts.code
    override var value by OnecProductTexts.value
}