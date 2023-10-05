// Create missing properties for Resource
MATCH (n:Resource) WHERE n.created_by IS NULL SET n.created_by = "00000000-0000-0000-0000-000000000000";
MATCH (n:Resource) WHERE n.organization_id IS NULL SET n.organization_id = "00000000-0000-0000-0000-000000000000";
MATCH (n:Resource) WHERE n.observatory_id IS NULL SET n.observatory_id = "00000000-0000-0000-0000-000000000000";
MATCH (n:Resource) WHERE n.extraction_method IS NULL SET n.extraction_method = "UNKNOWN";
