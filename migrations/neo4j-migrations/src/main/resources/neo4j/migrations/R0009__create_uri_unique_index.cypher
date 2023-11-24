// Create unique index for uri
CREATE CONSTRAINT node_cnstr_uniq_for_thing_on_uri IF NOT EXISTS FOR (n:Thing) REQUIRE n.uri IS UNIQUE;
