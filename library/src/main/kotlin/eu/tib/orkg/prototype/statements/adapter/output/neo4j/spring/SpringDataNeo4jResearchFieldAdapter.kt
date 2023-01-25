package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jProblemsPerField
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResearchFieldRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ResourceId
import eu.tib.orkg.prototype.statements.spi.ProblemsPerField
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class SpringDataNeo4jResearchFieldAdapter(
    private val neo4jRepository: Neo4jResearchFieldRepository,
) : ResearchFieldRepository {
    override fun findById(id: ResourceId): Optional<Resource> =
        neo4jRepository.findById(id).map { it.toResource() }

    override fun getResearchProblemsOfField(fieldId: ResourceId, pageable: Pageable): Page<ProblemsPerField> =
        neo4jRepository.getResearchProblemsOfField(fieldId, pageable).map { it.toProblemsPerField() }

    override fun getContributorIdsFromResearchFieldAndIncludeSubfields(
        id: ResourceId,
        pageable: Pageable
    ): Page<ContributorId> =
        neo4jRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id, pageable)

    override fun getPapersIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getPapersIncludingSubFields(id, pageable)
            .map { it.toResource() }

    override fun getPapersIncludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getPapersIncludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getComparisonsIncludingSubFields(id, pageable)
            .map { it.toResource() }

    override fun getComparisonsIncludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsIncludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            .map { it.toResource() }

    override fun getProblemsIncludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getProblemsIncludingSubFields(id, pageable)
            .map { it.toResource() }

    override fun getProblemsIncludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsIncludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getProblemsIncludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsIncludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            .map { it.toResource() }

    override fun getContributorIdsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<ContributorId> =
        neo4jRepository.getContributorIdsExcludingSubFields(id, pageable)

    override fun getPapersExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getPapersExcludingSubFields(id, pageable).map { it.toResource() }

    override fun getPapersExcludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getPapersExcludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getComparisonsExcludingSubFields(id, pageable)
            .map { it.toResource() }

    override fun getComparisonsExcludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsExcludingSubFieldsWithFlags(id, featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsExcludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id, unlisted, pageable)
            .map { it.toResource() }

    override fun getProblemsExcludingSubFields(id: ResourceId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getProblemsExcludingSubFields(id, pageable).map { it.toResource() }

    override fun getProblemsExcludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsExcludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getProblemsExcludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsExcludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun findResearchFieldsWithBenchmarks(): Iterable<Resource> =
        neo4jRepository.findResearchFieldsWithBenchmarks().map { it.toResource() }

    override fun getVisualizationsIncludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsIncludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsIncludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsIncludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListIncludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListIncludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getVisualizationsExcludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsExcludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsExcludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsExcludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListExcludingSubFieldsWithFlags(
        id: ResourceId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListExcludingSubFieldsWithFlags(
            id,
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(
        id: ResourceId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(
            id,
            unlisted,
            pageable
        ).map { it.toResource() }

    private fun Neo4jProblemsPerField.toProblemsPerField() =
        ProblemsPerField(
            problem = problem.toResource(),
            papers = papers
        )
}
