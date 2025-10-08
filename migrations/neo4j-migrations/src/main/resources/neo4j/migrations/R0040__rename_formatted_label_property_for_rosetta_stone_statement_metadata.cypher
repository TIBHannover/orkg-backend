MATCH (n:RosettaStoneStatementMetadata)
WHERE n.formatted_label IS NOT NULL
SET n.dynamic_label = n.formatted_label
REMOVE n.formatted_label;
