MATCH (s)-[p]->(o)
WITH s, p.predicate_id AS pid, o, TAIL(COLLECT(p)) AS duplicates
FOREACH(x in duplicates | DELETE x)
