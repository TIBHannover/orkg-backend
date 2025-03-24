MATCH (p:LiteratureListPublished:LatestVersion)
REMOVE p:LatestVersion;

MATCH (h:LiteratureList)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(p:LiteratureListPublished)
WITH h, COLLECT(p) AS published
WITH h, apoc.coll.sortNodes(published, "created_at")[0] AS latestVersion
SET latestVersion:LatestVersion;
