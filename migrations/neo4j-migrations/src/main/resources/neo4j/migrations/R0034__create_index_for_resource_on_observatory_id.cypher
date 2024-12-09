CREATE INDEX node_idx_for_resource_on_observatory_id IF NOT EXISTS FOR (n:Resource) ON n.observatory_id;
