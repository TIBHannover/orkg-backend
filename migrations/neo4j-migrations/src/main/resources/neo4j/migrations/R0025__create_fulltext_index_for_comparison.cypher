CREATE FULLTEXT INDEX fulltext_idx_for_comparison_on_label IF NOT EXISTS FOR (n:Comparison) ON EACH [n.label];
