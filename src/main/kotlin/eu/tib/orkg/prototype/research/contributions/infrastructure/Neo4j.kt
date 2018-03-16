package eu.tib.orkg.prototype.research.contributions.infrastructure

import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import org.springframework.context.annotation.Profile
import org.springframework.data.neo4j.repository.Neo4jRepository

@NodeEntity(label = "ResearchMethod")
class Neo4jResearchMethod {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Property(name = "value")
    var value: String? = null
}

@NodeEntity(label = "ResearchResult")
class Neo4jResearchResult {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Property(name = "value")
    var value: String? = null
}

@NodeEntity(label = "ResearchProblem")
class Neo4jResearchProblem {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Property(name = "value")
    var value: String? = null
}

@NodeEntity(label = "ResearchContribution")
class Neo4jResearchContribution {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Property(name = "value")
    var value: String? = null

    @Relationship(type = "employs")
    var methods: List<Neo4jResearchMethod>? = null

    @Relationship(type = "addresses")
    var problems: List<Neo4jResearchProblem>? = null

    @Relationship(type = "yields")
    var results: List<Neo4jResearchResult>? = null
}

@Profile("neo4j")
interface Neo4jResearchContributionRepository :
    Neo4jRepository<Neo4jResearchContribution, Long> {
    override fun findAll(): MutableIterable<Neo4jResearchContribution>?
}
