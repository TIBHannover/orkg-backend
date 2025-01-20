CREATE INDEX node_idx_for_id_counter_on_id IF NOT EXISTS FOR (n:_IdCounter) ON n.id;
