package eu.tib.orkg.prototype.research.contributions.infrastructure

import eu.tib.orkg.prototype.research.contributions.domain.model.ResearchContribution
import eu.tib.orkg.prototype.research.contributions.domain.model.ResearchContributionRepository
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.neo4j.annotation.Query
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.stereotype.Repository


@NodeEntity(label = "ResearchContribution")
class Neo4jResearchContribution {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Property(name = "value")
    var value: String? = null
}


interface Neo4jResearchContributionRepository :
    Neo4jRepository<Neo4jResearchContribution, Long> {
    @Query("MATCH (n:ResearchContribution) RETURN n")
    override fun findAll(): MutableIterable<Neo4jResearchContribution>?
}

@Repository
@Profile("neo4j")
class Neo4jResearchContributionRepositoryWrapper :
    ResearchContributionRepository {

    @Autowired
    private lateinit var repository: Neo4jResearchContributionRepository

    override fun findAll(): Collection<ResearchContribution> {
        val results = repository.findAll()
        results?.let {
            return results.map { ResearchContribution(it.value ?: "") }
        }
        return setOf()
    }
}
