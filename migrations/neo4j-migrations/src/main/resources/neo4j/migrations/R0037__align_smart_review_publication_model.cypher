MATCH (srp:`SmartReviewPublished`)<-[:`RELATED` {predicate_id: 'hasPublishedVersion'}]-(srl:`SmartReview`)
WITH srl, apoc.coll.sortNodes(collect(srp), 'created_at') AS published
WITH srl, published[0] AS latestVersion, TAIL(published) AS outdatedVersions
SET latestVersion:LatestVersion
WITH outdatedVersions
UNWIND outdatedVersions AS outdated
REMOVE outdated:LatestVersion;
