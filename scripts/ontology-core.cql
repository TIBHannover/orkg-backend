CREATE
  (do:ResearchProblem {value:"Design of ontologies"}),
  (fp:ResearchProblem {value:"Fermat's last theorem (conjecture)"}),
  (ke:ResearchProblem {value:"Knowledge Engineering"}),
  (tsw:ResearchProblem {value:"Taniyama-Shimura-Weil conjecture"}),

  (cs:ResearchMethod {value:"Case studies"}),
  (mp:ResearchMethod {value:"Mathematical proof"}),

  (dco:ResearchResult {value:"Design criteria for ontologies"}),
  (mt:ResearchResult {value:"Modularity theorem"}),

  (gruber:ResearchContribution {value:"Gruber's design of ontologies"}),
  (gruber)-[:addresses]->(do),
  (gruber)-[:addresses]->(ke),
  (gruber)-[:employs]->(cs),
  (gruber)-[:yields]->(dco),

  (wiles:ResearchContribution {value:"Wiles's proof of Fermat's last theorem"}),
  (wiles)-[:addresses]->(fp),
  (wiles)-[:addresses]->(tsw),
  (wiles)-[:employs]->(mp),
  (wiles)-[:yields]->(mt)
RETURN
  do, fp, ke, tsw,
  cs, mp,
  dco, mt,
  gruber, wiles;
