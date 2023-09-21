CREATE FULLTEXT INDEX fulltext_idx_for_class_on_label IF NOT EXISTS FOR (n:Class) ON EACH [n.label];
CREATE FULLTEXT INDEX fulltext_idx_for_literal_on_label IF NOT EXISTS FOR (n:Literal) ON EACH [n.label];
CREATE FULLTEXT INDEX fulltext_idx_for_predicate_on_label IF NOT EXISTS FOR (n:Predicate) ON EACH [n.label];
CREATE FULLTEXT INDEX fulltext_idx_for_resource_on_label IF NOT EXISTS FOR (n:Resource) ON EACH [n.label];
