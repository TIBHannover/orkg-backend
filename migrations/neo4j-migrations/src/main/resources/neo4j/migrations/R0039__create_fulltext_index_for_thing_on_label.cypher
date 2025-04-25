CREATE FULLTEXT INDEX fulltext_idx_for_thing_on_label IF NOT EXISTS FOR (n:Thing) ON EACH [n.label];
