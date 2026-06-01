package org.orkg.migrations.neo4j

@Suppress("ClassName", "unused")
class R0048__DropCustomTimestampParsingFunction :
    AbstractCustomProcedureMigration(
        migrationQueryFile = "R0048__drop_custom_timestamp_parsing_function",
        validationQuery = "RETURN true",
    )
