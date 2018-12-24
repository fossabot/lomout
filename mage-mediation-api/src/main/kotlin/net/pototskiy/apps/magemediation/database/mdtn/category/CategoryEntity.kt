package net.pototskiy.apps.magemediation.database.mdtn.category

import net.pototskiy.apps.magemediation.database.mdtn.MediumDataEntity
import org.jetbrains.exposed.dao.EntityID

abstract class CategoryEntity(id: EntityID<Int>): MediumDataEntity(id) {
    abstract var entityID: Long
}