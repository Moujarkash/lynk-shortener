package com.mod.database

import org.jetbrains.exposed.sql.StringColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

class CitextColumnType: StringColumnType() {
    override fun valueFromDB(value: Any): String = when (value) {
        is PGobject -> value.toString()
        else -> super.valueFromDB(value)
    }

    override fun sqlType(): String = "citext"
}

fun Table.citext(name: String) = registerColumn(name, CitextColumnType())