// Indexes on Resource
CREATE INDEX composite_idx_for_resource_on_visibility_and_created_at IF NOT EXISTS FOR (n:Resource) ON (n.visibility, n.created_at)
