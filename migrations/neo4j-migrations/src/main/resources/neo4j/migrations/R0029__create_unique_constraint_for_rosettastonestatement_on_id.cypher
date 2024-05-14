CREATE CONSTRAINT node_cnstr_uniq_for_rosettastonestatement_on_id IF NOT EXISTS FOR (n:RosettaStoneStatement) REQUIRE n.id IS UNIQUE;
