MATCH(p:Paper) SET p.featured=false RETURN p
MATCH(s:SmartReview) SET s.featured=false RETURN s
MATCH(p:Problem) SET p.featured=false RETURN p
MATCH(c:Comparison) SET c.featured=false RETURN c
MATCH(v:Visualization) SET v.featured=false RETURN v
MATCH(p:Paper) SET p.unlisted=false RETURN p
MATCH(s:SmartReview) SET s.unlisted=false RETURN s
MATCH(c:Comparison) SET c.unlisted=false RETURN c
MATCH(v:Visualization) SET v.unlisted=false RETURN v
MATCH(problem:Problem) SET problem.unlisted=false RETURN v
