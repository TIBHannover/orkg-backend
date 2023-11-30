CREATE FULLTEXT INDEX fulltext_idx_for_template_on_label IF NOT EXISTS FOR (n:NodeShape) ON EACH [n.label];
