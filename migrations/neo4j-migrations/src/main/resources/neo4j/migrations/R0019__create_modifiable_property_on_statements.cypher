MATCH (:Thing)-[r:RELATED]->(:Thing) SET r.modifiable = true;
