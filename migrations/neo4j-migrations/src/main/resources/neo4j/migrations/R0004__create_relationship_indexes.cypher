// Create relationship indexes for statements
CREATE INDEX rel_idx_for_related_on_statement_id IF NOT EXISTS FOR ()-[r:RELATED]-() ON (r.statement_id);
CREATE INDEX rel_idx_for_related_on_predicate_id IF NOT EXISTS FOR ()-[r:RELATED]-() ON (r.predicate_id);
