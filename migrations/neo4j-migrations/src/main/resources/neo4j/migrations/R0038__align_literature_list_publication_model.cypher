MATCH (p:LiteratureListPublished:LatestVersion)
REMOVE p:LatestVersion;

MATCH (h:LiteratureList)
WITH h, COLLECT {
    MATCH (h)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(p:LiteratureListPublished)
    RETURN p ORDER BY p.created_at DESC
} AS published
WITH h, HEAD(published) AS latestVersion
SET latestVersion:LatestVersion;
