// Create relationship indexes for statements
CREATE INDEX rel_idx_for_related_on_created_at IF NOT EXISTS FOR ()-[r:RELATED]-() ON (r.created_at);
CREATE INDEX rel_idx_for_related_on_created_by IF NOT EXISTS FOR ()-[r:RELATED]-() ON (r.created_by);
