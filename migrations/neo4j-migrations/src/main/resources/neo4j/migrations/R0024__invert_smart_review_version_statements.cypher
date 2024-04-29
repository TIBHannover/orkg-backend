MATCH (:SmartReviewPublished)-[r:RELATED]->(:SmartReview)
SET r.predicate_id = "hasPublishedVersion"
WITH r
CALL apoc.refactor.invert(r)
YIELD input, output
RETURN COUNT(output);
