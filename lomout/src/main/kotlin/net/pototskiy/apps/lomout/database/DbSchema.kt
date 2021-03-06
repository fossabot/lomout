package net.pototskiy.apps.lomout.database

import net.pototskiy.apps.lomout.api.database.DbEntityTable
import net.pototskiy.apps.lomout.api.database.EntityBooleans
import net.pototskiy.apps.lomout.api.database.EntityDateTimes
import net.pototskiy.apps.lomout.api.database.EntityDoubles
import net.pototskiy.apps.lomout.api.database.EntityLongs
import net.pototskiy.apps.lomout.api.database.EntityTexts
import net.pototskiy.apps.lomout.api.database.EntityVarchars
import net.pototskiy.apps.lomout.api.entity.EntityTypeManager
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DbSchema {
    fun createSchema(entityTypeManager: EntityTypeManager) {
        DbEntityTable.entityTypeManager = entityTypeManager
        transaction {
            SchemaUtils.create(DbEntityTable)
            SchemaUtils.create(
                EntityVarchars,
                EntityLongs,
                EntityBooleans,
                EntityDoubles,
                EntityDateTimes,
                EntityTexts
            )
            SchemaUtils.create(PipelineSets)
        }
    }
}
