MATCH (h:Paper)-[r:RELATED*0.. {predicate_id: "hasPreviousVersion"}]->(p:PaperVersion)
WITH h, last(r) AS r, p
CALL apoc.refactor.from(r, h)
YIELD output
SET output.predicate_id = "hasPublishedVersion"
RETURN COUNT(output);

MATCH (h:Paper)
WITH h, COLLECT {
    MATCH (h)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(p:PaperVersion)
    RETURN p ORDER BY p.created_at DESC
} AS published
WITH h, HEAD(published) AS latestVersion
SET latestVersion:LatestVersion;
