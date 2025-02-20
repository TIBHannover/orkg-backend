package org.orkg.migrations.neo4j

@Suppress("ClassName", "unused")
class R0031__CreateCustomTimestampParsingFunction :
    AbstractCustomProcedureMigration(
        migrationQueryFile = "R0031__create_custom_timestamp_parsing_function",
        validationQuery = "RETURN custom.parseIsoOffsetDateTime('2012-12-23T21:15:05.645313+02:00', 'ms')",
    )
