CREATE FULLTEXT INDEX fulltext_idx_for_paper_on_label IF NOT EXISTS FOR (n:Paper) ON EACH [n.label];
