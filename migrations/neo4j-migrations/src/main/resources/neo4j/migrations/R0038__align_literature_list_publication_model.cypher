MATCH (llp:`LiteratureListPublished`)<-[:`RELATED` {predicate_id: 'hasPublishedVersion'}]-(lll:`LiteratureList`)
WITH lll, apoc.coll.sortNodes(collect(llp), 'created_at') AS published
WITH lll, published[0] AS latestVersion, TAIL(published) AS outdatedVersions
SET latestVersion:LatestVersion
WITH outdatedVersions
UNWIND outdatedVersions AS outdated
REMOVE outdated:LatestVersion;
