MATCH (p:SmartReviewPublished:LatestVersion)
REMOVE p:LatestVersion;

MATCH (h:SmartReview)
WITH h, COLLECT {
    MATCH (h)-[:RELATED {predicate_id: "hasPublishedVersion"}]->(p:SmartReviewPublished)
    RETURN p ORDER BY p.created_at DESC
} AS published
WITH h, HEAD(published) AS latestVersion
SET latestVersion:LatestVersion;
