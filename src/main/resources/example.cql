CREATE
// Predicates
    (:Predicate {predicate_id: 'P123', label: 'addresses'}),
    (:Predicate {predicate_id: 'P234', label: 'yields'}),
    (:Predicate {predicate_id: 'Pabc', label: 'employs'}),
// Resources
    (:Resource {resource_id: 'b0b0b0',
               label:       "Gruber's design of ontologies"}),
    (:Resource {resource_id: '0a0a0a',
                  label:       "Wiles's proof of Fermat's last theorem"}),
    (:Resource {resource_id: '1234',
                  label:       'Mathematical proof'}),
    (:Resource {resource_id: '2345',
               label:       'Modularity theorem'}),
    (:Resource {resource_id: '3456',
               label:       "Fermat's last theorem (conjecture)"}),
    (:Resource {resource_id: '4567',
               label:       'Taniyama-Shimura-Weil conjecture'}),
    (:Resource {resource_id: '5678',
               label:       'Design criteria for ontologies'}),
    (:Resource {resource_id: '6789',
               label:       'Knowledge Engineering'}),
    (:Resource {resource_id: '789a',
               label:       'Design of ontologies'}),
    (:Resource {resource_id: '89AB',
               label:       'Case studies'});

MATCH (wp:Resource),
      (mp:Resource),
      (tsw:Resource),
      (flt:Resource),
      (mt:Resource)
    WHERE wp.resource_id = '0a0a0a'
    AND mp.resource_id = '1234'
    AND tsw.resource_id = '4567'
    AND flt.resource_id = '3456'
    AND mt.resource_id = '2345'
CREATE (wp)-[:Pabc]->(mp),
       (wp)-[:P123]->(tsw),
       (wp)-[:P123]->(flt),
       (wp)-[:P234]->(mt)
;

MATCH (gd:Resource),
      (cs:Resource),
      (do:Resource),
      (ke:Resource),
      (dco:Resource)
    WHERE gd.resource_id = 'b0b0b0'
    AND cs.resource_id = '89AB'
    AND do.resource_id = '789a'
    AND ke.resource_id = '6789'
    AND dco.resource_id = '5678'
CREATE (gd)-[:Pabc]->(cs),
       (gd)-[:P123]->(do),
       (gd)-[:P123]->(ke),
       (gd)-[:P234]->(dco)
;

// "Semantics" extension labels
MATCH (p:Predicate) SET p:rdf__Predicate RETURN *;
