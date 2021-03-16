package eu.tib.orkg.prototype.statements.infrastructure.neo4j

import com.fasterxml.jackson.annotation.JsonProperty
import eu.tib.orkg.prototype.auth.persistence.UserEntity
import eu.tib.orkg.prototype.auth.service.UserRepository
import eu.tib.orkg.prototype.contributions.domain.model.Contributor
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.domain.model.VisualizationService
import eu.tib.orkg.prototype.statements.domain.model.neo4j.Neo4jVisualizationRepository
import eu.tib.orkg.prototype.statements.domain.model.neo4j.VisualizationResAndMetaInfo
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Neo4jVisualizationService(
    private val neo4jVisualizationRepository: Neo4jVisualizationRepository,
    private val userRepository: UserRepository
) : VisualizationService {
    override fun getVisExcludingSubResearchFields(id: ResourceId, pageable: Pageable): Page<VisualizationWithMetaAndProfile> {
        val visualizationIdsWithMetaInfo =
            neo4jVisualizationRepository.findVisResourceIdsExcludingSubResearchFields(id, pageable)
        return getVisualizationsWithProfile(visualizationIdsWithMetaInfo, pageable)
    }

    override fun getVisIncludingSubResearchFields(id: ResourceId, pageable: Pageable): Page<VisualizationWithMetaAndProfile> {
        val visualizationIdsWithMetaInfo =
            neo4jVisualizationRepository.findVisResourceIdsIncludingSubResearchFields(id, pageable)
        return getVisualizationsWithProfile(visualizationIdsWithMetaInfo, pageable)
    }

    private fun getVisualizationsWithProfile(visualizationAndMetaInfo: Page<VisualizationResAndMetaInfo>, pageable: Pageable): Page<VisualizationWithMetaAndProfile> {
        val refinedVisualizations = mutableListOf<VisualizationWithMetaAndProfile>()

        val userIdList = visualizationAndMetaInfo.content.map { UUID.fromString(it.createdBy) }.toTypedArray()

        val mapValues = userRepository.findByIdIn(userIdList).map(UserEntity::toContributor).groupBy(
            Contributor::id)

        visualizationAndMetaInfo.forEach { vis ->
            val contributor = mapValues[ContributorId(vis.createdBy)]?.first()

            refinedVisualizations.add(VisualizationWithMetaAndProfile(vis.id, vis.label,
                vis.createdAt, vis.comparisonId, vis.description, vis.comparisonLabel,
                Profile(contributor?.id, contributor?.name, contributor?.gravatarId, contributor?.avatarURL)))
        }

        return PageImpl(refinedVisualizations, pageable, refinedVisualizations.size.toLong())
    }
}

/**
 * Data class containing visualization
 * data,meta information and profile
 * details
 */
data class VisualizationWithMetaAndProfile(
    val id: String? = null,
    val label: String? = null,
    @JsonProperty("created_at")
    val createdAt: String? = null,
    @JsonProperty("comparison_id")
    val comparisonId: String? = null,
    val description: String? = null,
    @JsonProperty("comparison_label")
    val comparisonLabel: String? = null,
    val profile: Profile? = null
)
