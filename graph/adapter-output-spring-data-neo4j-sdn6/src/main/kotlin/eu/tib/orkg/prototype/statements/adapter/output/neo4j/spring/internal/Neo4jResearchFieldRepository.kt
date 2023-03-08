package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.paperswithcode.adapters.output.persistence.neo4j.*
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

private const val unlisted = "${'$'}unlisted"
private const val featured = "${'$'}featured"
private const val fieldId = "${'$'}fieldId"
private const val id = "${'$'}id"

interface Neo4jResearchFieldRepository :
    Neo4jRepository<Neo4jResource, Long> {
    @Query("""MATCH (field:ResearchField {resource_id: $id}) RETURN field""")
    fun findById(id: ResourceId): Optional<Neo4jResource>

    @Query("""MATCH (field:ResearchField {resource_id: $fieldId})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing)
                    WITH COUNT(paper) AS papers, problem
                    RETURN DISTINCT problem, papers""",
        countQuery = """MATCH (field:ResearchField {resource_id: $fieldId})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing)
                        WITH COUNT(paper) AS papers, problem
                        RETURN COUNT(papers) AS cnt"""
    )
    fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<Neo4jProblemsPerField>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items UNWIND items AS orkgusers RETURN DISTINCT orkgusers""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items UNWIND items AS orkgusers RETURN COUNT(DISTINCT orkgusers) AS cnt""")
    fun getContributorIdsFromResearchFieldAndIncludeSubfields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE (p.featured=true OR p.featured = false OR p.unlisted = false) AND resField IN all_research_fields WITH DISTINCT p, p.created_at as created_at, p.resource_id AS resource_id, p.label AS label, p.created_by AS created_by RETURN p",
        countQuery = "MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper)-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE (p.featured=true OR p.featured = false OR p.unlisted = false) AND resField IN all_research_fields RETURN COUNT(p) AS cnt")
    fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper {featured:$featured, unlisted:$unlisted})-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT p, p.created_at AS created_at, p.resource_id AS resource_id, p.label AS label, p.created_by AS created_by RETURN p",
        countQuery = "MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper {featured:$featured, unlisted:$unlisted})-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(p) AS cnt")
    fun getPapersIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper {unlisted:$unlisted})-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT p, p.created_at AS created_at, p.resource_id AS resource_id, p.label AS label, p.created_by AS created_by RETURN p",
        countQuery = "MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(p:Paper {unlisted:$unlisted})-[:RELATED{predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(p) AS cnt")
    fun getPapersIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(orkgcomparisons: Comparison )-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE (orkgcomparisons.featured=true OR orkgcomparisons.featured = false OR orkgcomparisons.unlisted = false) AND resField IN all_research_fields WITH DISTINCT orkgcomparisons, orkgcomparisons.resource_id AS resource_id, orkgcomparisons.label AS label, orkgcomparisons.created_by AS created_by, orkgcomparisons.created_at AS created_at RETURN orkgcomparisons""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(orkgcomparisons: Comparison )-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE (orkgcomparisons.featured=true OR orkgcomparisons.featured = false OR orkgcomparisons.unlisted = false) AND resField IN all_research_fields RETURN COUNT(DISTINCT orkgcomparisons) AS cnt""")
    fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(orkgcomparisons: Comparison {featured: $featured, unlisted: $unlisted} )-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT orkgcomparisons, orkgcomparisons.resource_id AS resource_id, orkgcomparisons.label AS label, orkgcomparisons.created_by AS created_by, orkgcomparisons.created_at AS created_at RETURN orkgcomparisons""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(orkgcomparisons: Comparison {featured: $featured, unlisted: $unlisted} )-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT orkgcomparisons) AS cnt""")
    fun getComparisonsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(orkgcomparisons: Comparison {unlisted: $unlisted} )-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT orkgcomparisons, orkgcomparisons.resource_id AS resource_id, orkgcomparisons.label AS label, orkgcomparisons.created_by AS created_by, orkgcomparisons.created_at AS created_at RETURN orkgcomparisons""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH(orkgcomparisons: Comparison {unlisted: $unlisted} )-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT orkgcomparisons) AS cnt""")
    fun getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (orkgproblems:Thing)<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE (orkgproblems.featured=true OR orkgproblems.featured = false OR orkgproblems.unlisted = false) AND resField IN all_research_fields WITH DISTINCT orkgproblems, orkgproblems.resource_id AS resource_id, orkgproblems.created_at AS created_at, orkgproblems.label AS label, orkgproblems.created_by AS created_by RETURN orkgproblems""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (orkgproblems:Thing)<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE (orkgproblems.featured=true OR orkgproblems.featured = false OR orkgproblems.unlisted = false) AND resField IN all_research_fields RETURN COUNT(DISTINCT orkgproblems)""")
    fun getProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (orkgproblems:Thing {featured: $featured, unlisted: $unlisted})<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT orkgproblems, orkgproblems.resource_id AS resource_id, orkgproblems.created_at AS created_at, orkgproblems.label AS label, orkgproblems.created_by AS created_by RETURN orkgproblems""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (orkgproblems:Thing {featured: $featured, unlisted: $unlisted})<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT orkgproblems)""")
    fun getProblemsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (orkgproblems:Thing {unlisted: $unlisted})<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT orkgproblems, orkgproblems.resource_id AS resource_id, orkgproblems.created_at AS created_at, orkgproblems.label AS label, orkgproblems.created_by AS created_by RETURN orkgproblems""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (orkgproblems:Thing {unlisted: $unlisted})<-[:RELATED {predicate_id: 'P32'}]-(contr1: Contribution)<-[:RELATED{predicate_id: 'P31'}]-(papers1:Paper)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT orkgproblems)""")
    fun getProblemsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: $id}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN orkgusers""",
        countQuery = """MATCH(comparison1: Comparison)-[related:RELATED]->(contribution1:Contribution)<-[:RELATED{predicate_id: "P31"}]-(p1:Paper)-[:RELATED {predicate_id: 'P30'}]->(:ResearchField{resource_id: $id}) 
                    WITH COLLECT(comparison1.created_by) + COLLECT(contribution1.created_by) + COLLECT(p1.created_by) AS items 
                    UNWIND items AS orkgusers
                    RETURN COUNT(orkgusers) as cnt""")
    fun getContributorIdsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<ContributorId>

    @Query("MATCH(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:$id}) WHERE (NOT EXISTS(paper.featured) OR paper.featured = false) AND (NOT EXISTS(paper.unlisted) OR paper.unlisted = false) WITH DISTINCT paper, paper.created_at AS created_at, paper.resource_id AS resource_id, paper.label AS label, paper.created_by AS created_by RETURN paper",
        countQuery = "MATCH(paper:Paper)-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:$id}) WHERE (NOT EXISTS(paper.featured) OR paper.featured = false) AND (NOT EXISTS(paper.unlisted) OR paper.unlisted = false) RETURN COUNT(paper) AS cnt")
    fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("MATCH(paper:Paper {featured: $featured, unlisted: $unlisted} )-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:$id}) WITH DISTINCT paper, paper.created_at AS created_at, paper.resource_id AS resource_id, paper.label AS label, paper.created_by AS created_by RETURN paper",
        countQuery = "MATCH(paper:Paper {featured: $featured, unlisted: $unlisted})-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:$id}) RETURN COUNT(paper) AS cnt")
    fun getPapersExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("MATCH(paper:Paper {unlisted: $unlisted} )-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:$id}) WITH DISTINCT paper, paper.created_at AS created_at, paper.resource_id AS resource_id, paper.label AS label, paper.created_by AS created_by RETURN paper",
        countQuery = "MATCH(paper:Paper {unlisted: $unlisted})-[:RELATED {predicate_id: 'P30'}]->(r: ResearchField{resource_id:$id}) RETURN COUNT(paper) AS cnt")
    fun getPapersExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = "MATCH(comparison: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: $id}) WHERE (comparison.featured=true OR comparison.featured = false OR comparison.unlisted = false) WITH DISTINCT comparison, comparison.resource_id AS resource_id, comparison.label AS label, comparison.created_by AS created_by, comparison.created_at AS created_at RETURN comparison",
        countQuery = "MATCH(comparison: Comparison)-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: $id}) WHERE (comparison.featured=true OR comparison.featured = false OR comparison.unlisted = false) RETURN COUNT(comparison)")
    fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query(value = "MATCH(comparison: Comparison {featured: $featured, unlisted: $unlisted})-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: $id}) WITH DISTINCT comparison, comparison.resource_id AS resource_id, comparison.label AS label, comparison.created_by AS created_by, comparison.created_at AS created_at RETURN comparison",
        countQuery = "MATCH(comparison: Comparison {featured: $featured, unlisted: $unlisted})-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: $id}) RETURN COUNT(comparison)")
    fun getComparisonsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query(value = "MATCH(comparison: Comparison {unlisted: $unlisted})-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: $id}) WITH DISTINCT comparison, comparison.resource_id AS resource_id, comparison.label AS label, comparison.created_by AS created_by, comparison.created_at AS created_at RETURN comparison",
        countQuery = "MATCH(comparison: Comparison {unlisted: $unlisted})-[related:RELATED]->(contr:Contribution)<-[:RELATED{predicate_id: 'P31'}]-(p:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField{resource_id: $id}) RETURN COUNT(comparison)")
    fun getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (field:ResearchField {resource_id: $id})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) WHERE (problem.featured=true OR problem.featured = false OR problem.unlisted = false) WITH DISTINCT problem, problem.created_at AS created_at, problem.resource_id AS resource_id, problem.created_by AS created_by RETURN problem """,
        countQuery = """MATCH (field:ResearchField {resource_id: $id})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing) WHERE (problem.featured=true OR problem.featured = false OR problem.unlisted = false) RETURN COUNT(DISTINCT problem) AS cnt""")
    fun getProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (field:ResearchField {resource_id: $id})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing {featured: $featured, unlisted: $unlisted} ) WITH DISTINCT problem, problem.created_at AS created_at, problem.resource_id AS resource_id, problem.created_by AS created_by RETURN problem """,
        countQuery = """MATCH (field:ResearchField {resource_id: $id})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing {featured: $featured, unlisted: $unlisted} )  RETURN COUNT(DISTINCT problem) AS cnt""")
    fun getProblemsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (field:ResearchField {resource_id: $id})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing {unlisted: $unlisted} ) WITH DISTINCT problem, problem.created_at AS created_at, problem.resource_id AS resource_id, problem.created_by AS created_by RETURN problem """,
        countQuery = """MATCH (field:ResearchField {resource_id: $id})<-[:RELATED {predicate_id: 'P30'}]-(paper:Paper)-[:RELATED {predicate_id: 'P31'}]->(cont:Contribution)-[:RELATED {predicate_id: 'P32'}]->(problem:Thing {unlisted: $unlisted} )  RETURN COUNT(DISTINCT problem) AS cnt""")
    fun getProblemsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField) RETURN DISTINCT r""",
        countQuery = """MATCH (:$BENCHMARK_CLASS)<-[:RELATED {predicate_id: '$BENCHMARK_PREDICATE'}]-(:Contribution)<-[:RELATED {predicate_id: 'P31'}]-(:Paper)-[:RELATED {predicate_id: 'P30'}]->(r:ResearchField) RETURN COUNT(DISTINCT r) AS cnt""")
    fun findResearchFieldsWithBenchmarks(): Iterable<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (v:Visualization {featured: $featured, unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT v, v.resource_id AS resource_id, v.label AS label, v.created_by AS created_by, v.created_at AS created_at RETURN v""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (v:Visualization {featured: $featured, unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT v) AS cnt""")
    fun getVisualizationsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (v:Visualization {unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(resField) WHERE resField IN all_research_fields WITH DISTINCT v, v.resource_id AS resource_id, v.label AS label, v.created_by AS created_by, v.created_at AS created_at RETURN v""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (v:Visualization {unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(resField) WHERE resField IN all_research_fields RETURN COUNT(DISTINCT v) AS cnt""")
    fun getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getSmartReviewsIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getLiteratureListIncludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (research:ResearchField)<-[:RELATED* 0.. {predicate_id: 'P36'}]-(research1:ResearchField{resource_id: $id}) WITH COLLECT (research) + COLLECT(research1) AS all_research_fields MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(resField) WHERE resField IN all_research_fields AND 'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (v:Visualization {featured: $featured, unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(r: ResearchField {resource_id: $id}) WITH DISTINCT v, v.resource_id AS resource_id, v.label AS label, v.created_by AS created_by, v.created_at AS created_at RETURN v""",
        countQuery = """MATCH (v:Visualization {featured: $featured, unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(r: ResearchField {resource_id: $id}) RETURN COUNT(DISTINCT v) AS cnt""")
    fun getVisualizationsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (v:Visualization {unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(r: ResearchField {resource_id: $id}) WITH DISTINCT v, v.resource_id AS resource_id, v.label AS label, v.created_by AS created_by, v.created_at AS created_at RETURN v""",
        countQuery = """MATCH (v:Visualization {unlisted: $unlisted})<-[:RELATED {predicate_id: 'hasVisualization'}]-(comparison1: Comparison)-[related:RELATED {predicate_id: 'hasSubject'}]->(r: ResearchField {resource_id: $id}) RETURN COUNT(DISTINCT v) AS cnt""")
    fun getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE 'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE  'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getSmartReviewsExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE 'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE  'SmartReviewPublished' IN LABELS(r) AND 'SmartReview' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE 'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (r:Resource {featured: $featured, unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE  'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getLiteratureListExcludingSubFieldsWithFlags(id: ResourceId, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>

    @Query("""MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE 'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) WITH DISTINCT r, r.resource_id AS resource_id, r.label AS label, r.created_by AS created_by, r.created_at AS created_at RETURN r""",
        countQuery = """MATCH (r:Resource {unlisted: $unlisted})-[:RELATED]->(r1: Resource)-[:RELATED {predicate_id: 'P30'}]->(research: ResearchField {resource_id: $id}) WHERE  'LiteratureListPublished' IN LABELS(r) AND 'LiteratureList' IN LABELS(r1) RETURN COUNT(DISTINCT r) AS cnt""")
    fun getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(id: ResourceId, unlisted: Boolean, pageable: Pageable): Page<Neo4jResource>
}

data class Neo4jProblemsPerField(
    val problem: Neo4jResource,
    val papers: Long
)