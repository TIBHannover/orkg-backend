CALL apoc.custom.declareProcedure(
    'subgraph(start :: ANY?, config :: MAP) :: (relationships :: LIST? OF RELATIONSHIP?)',
    'CALL apoc.path.subgraphAll($start, $config) YIELD relationships WITH relationships UNWIND relationships AS rel WITH rel WHERE rel:RELATED RETURN COLLECT(rel) AS relationships',
    'read',
    'custom.subgraph(startNode <id>|Node|list, {maxLevel,relationshipFilter,labelFilter,bfs:true, filterStartNode:false, limit:-1, endNodes:[], terminatorNodes:[], sequence, beginSequenceAtStart:true}) yield relationships - expand the subgraph reachable from start node following relationships to max-level adhering to the label filters, returning all RELATED relationships within the subgraph'
);
