MATCH (n:RosettaStoneStatement)
WHERE n.version IS NULL
SET n.version = 0
RETURN COUNT(n);
