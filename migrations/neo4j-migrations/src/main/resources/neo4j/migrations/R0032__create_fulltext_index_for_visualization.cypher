CREATE FULLTEXT INDEX fulltext_idx_for_visualization_on_label IF NOT EXISTS FOR (n:Visualization) ON EACH [n.label];
