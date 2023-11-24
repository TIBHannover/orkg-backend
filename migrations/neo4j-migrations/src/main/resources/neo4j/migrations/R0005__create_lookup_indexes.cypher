// assume that version is ge 4.3

// Drop obsolete lookup index (from Neo4j 3.5)
DROP INDEX __org_neo4j_schema_index_label_scan_store_converted_to_token_index IF EXISTS;
// Create node label lookup index (Neo4j 4.3+)

CREATE LOOKUP INDEX global_node_label_lookup_index IF NOT EXISTS FOR (n) ON EACH labels(n);
// Create relationship type lookup index (Neo4j 4.3+)
CREATE LOOKUP INDEX global_rel_type_lookup_index IF NOT EXISTS FOR ()-[r]-() ON EACH type(r);
