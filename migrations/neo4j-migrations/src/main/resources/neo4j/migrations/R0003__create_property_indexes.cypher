// Indexes on Thing
CREATE INDEX node_idx_for_thing_on_id IF NOT EXISTS FOR (n:Thing) ON n.id;
CREATE INDEX node_idx_for_thing_on_label IF NOT EXISTS FOR (n:Thing) ON n.label;
CREATE INDEX node_idx_for_thing_on_created_at IF NOT EXISTS FOR (n:Thing) ON n.created_at;
CREATE INDEX node_idx_for_thing_on_created_by IF NOT EXISTS FOR (n:Thing) ON n.created_by;

// Indexes on other `id` properties
CREATE INDEX node_idx_for_resource_on_id IF NOT EXISTS FOR (n:Resource) ON n.id;
CREATE INDEX node_idx_for_predicate_on_id IF NOT EXISTS FOR (n:Predicate) ON n.id;
CREATE INDEX node_idx_for_class_on_id IF NOT EXISTS FOR (n:Class) ON n.id;
CREATE INDEX node_idx_for_literal_on_id IF NOT EXISTS FOR (n:Literal) ON n.id;

// Indexes on other `label` properties
CREATE INDEX node_idx_for_class_on_label IF NOT EXISTS FOR (n:Class) ON n.label;
CREATE INDEX node_idx_for_literal_on_label IF NOT EXISTS FOR (n:Literal) ON n.label;
CREATE INDEX node_idx_for_predicate_on_label IF NOT EXISTS FOR (n:Predicate) ON n.label;
CREATE INDEX node_idx_for_resource_on_label IF NOT EXISTS FOR (n:Resource) ON n.label;

// Indexes specific for `Resource` nodes
CREATE INDEX node_idx_for_resource_on_created_at IF NOT EXISTS FOR (n:Resource) ON n.created_at;
CREATE INDEX node_idx_for_resource_on_created_by IF NOT EXISTS FOR (n:Resource) ON n.created_by;
CREATE INDEX node_idx_for_resource_on_visibility IF NOT EXISTS FOR (n:Resource) ON n.visibility;

// Node indexes of special classes (for better query performance)
CREATE INDEX node_idx_for_contribution_on_created_by IF NOT EXISTS FOR (n:Contribution) on n.created_by;
CREATE INDEX node_idx_for_contribution_on_created_at IF NOT EXISTS FOR (n:Contribution) on n.created_at;
CREATE INDEX node_idx_for_paper_on_created_by IF NOT EXISTS FOR (n:Paper) on n.created_by;
CREATE INDEX node_idx_for_paper_on_created_at IF NOT EXISTS FOR (n:Paper) on n.created_at;
CREATE INDEX node_idx_for_problem_on_created_by IF NOT EXISTS FOR (n:Problem) on n.created_by;
CREATE INDEX node_idx_for_problem_on_created_at IF NOT EXISTS FOR (n:Problem) on n.created_at;
CREATE INDEX node_idx_for_visualization_on_created_at IF NOT EXISTS FOR (n:Visualization) on n.created_by;
CREATE INDEX node_idx_for_visualization_on_created_at IF NOT EXISTS FOR (n:Visualization) on n.created_at;
