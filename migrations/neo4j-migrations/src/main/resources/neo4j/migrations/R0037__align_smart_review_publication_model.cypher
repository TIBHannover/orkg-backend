MATCH (p:SmartReviewPublished:LatestVersion)
REMOVE p:LatestVersion;

MATCH (h:SmartReview)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(p:SmartReviewPublished)
WITH h, COLLECT(p) AS published
WITH h, apoc.coll.sortNodes(published, "created_at")[0] AS latestVersion
SET latestVersion:LatestVersion;
