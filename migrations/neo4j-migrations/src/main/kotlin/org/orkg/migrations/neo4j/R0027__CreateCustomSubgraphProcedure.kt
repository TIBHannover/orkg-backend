package org.orkg.migrations.neo4j

@Suppress("ClassName")
class R0027__CreateCustomSubgraphProcedure : AbstractCustomProcedureMigration(
    """
    CALL apoc.custom.installProcedure(
        'subgraph(start :: ANY?, config :: MAP) :: (relationships :: LIST? OF RELATIONSHIP?)',
        'CALL apoc.path.subgraphAll(${'$'}start, ${'$'}config) YIELD relationships WITH relationships UNWIND relationships AS rel WITH rel WHERE rel:RELATED RETURN COLLECT(rel) AS relationships',
        'orkg',
        'read',
        'custom.subgraph(startNode <id>|Node|list, {maxLevel,relationshipFilter,labelFilter,bfs:true, filterStartNode:false, limit:-1, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield relationships - expand the subgraph reachable from start node following relationships to max-level adhering to the label filters, returning all RELATED relationships within the subgraph'
    );
    """.trimIndent(),
    validationQuery = "CALL custom.subgraph([], {})",
    version = 1
)
