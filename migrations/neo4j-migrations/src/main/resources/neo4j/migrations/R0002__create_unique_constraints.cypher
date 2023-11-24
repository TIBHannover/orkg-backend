// Unique Constraints
CREATE CONSTRAINT node_cnstr_uniq_for_thing_on_id IF NOT EXISTS FOR (n:Thing) REQUIRE n.id IS UNIQUE;
