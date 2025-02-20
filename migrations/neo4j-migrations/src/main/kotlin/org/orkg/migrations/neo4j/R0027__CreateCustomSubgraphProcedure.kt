package org.orkg.migrations.neo4j

@Suppress("ClassName", "unused")
class R0027__CreateCustomSubgraphProcedure :
    AbstractCustomProcedureMigration(
        migrationQueryFile = "R0027__create_custom_subgraph_procedure",
        validationQuery = "CALL custom.subgraph([], {})",
    )
