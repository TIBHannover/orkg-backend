package eu.tib.orkg.prototype.core.statements.adapters.output

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ObservatoryId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.Stats
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ChangeLogResponse
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jStatsRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.ObservatoryResources
import eu.tib.orkg.prototype.statements.domain.model.neo4j.TopContributorIdentifiers
import eu.tib.orkg.prototype.statements.domain.model.neo4j.TrendingResearchProblems
import eu.tib.orkg.prototype.statements.ports.StatsRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.lang.IllegalStateException
import java.time.LocalDate
import java.util.UUID

class StatsPersistenceAdapter(
    private val neo4jStatsRepository: Neo4jStatsRepository,
): StatsRepository {
    //Implementing the methods here seem tricky as most of them use userRepository
    //So still contemplating how to proceed.
}
