package eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring

import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jProblemsPerField
import eu.tib.orkg.prototype.statements.adapter.output.neo4j.spring.internal.Neo4jResearchFieldRepository
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
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
    override fun findById(id: ThingId): Optional<Resource> =
        neo4jRepository.findById(id.toResourceId()).map { it.toResource() }

    override fun getResearchProblemsOfField(fieldId: ThingId, pageable: Pageable): Page<ProblemsPerField> =
        neo4jRepository.getResearchProblemsOfField(fieldId.toResourceId(), pageable).map { it.toProblemsPerField() }

    override fun getContributorIdsFromResearchFieldAndIncludeSubfields(
        id: ThingId,
        pageable: Pageable
    ): Page<ContributorId> =
        neo4jRepository.getContributorIdsFromResearchFieldAndIncludeSubfields(id.toResourceId(), pageable)

    override fun getPapersIncludingSubFields(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getPapersIncludingSubFields(id.toResourceId(), pageable)
            .map { it.toResource() }

    override fun getPapersIncludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersIncludingSubFieldsWithFlags(id.toResourceId(), featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getPapersIncludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersIncludingSubFieldsWithoutFeaturedFlag(id.toResourceId(), unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getComparisonsIncludingSubFields(id.toResourceId(), pageable)
            .map { it.toResource() }

    override fun getComparisonsIncludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsIncludingSubFieldsWithFlags(id.toResourceId(), featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsIncludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsIncludingSubFieldsWithoutFeaturedFlag(id.toResourceId(), unlisted, pageable)
            .map { it.toResource() }

    override fun getProblemsIncludingSubFields(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getProblemsIncludingSubFields(id.toResourceId(), pageable)
            .map { it.toResource() }

    override fun getProblemsIncludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsIncludingSubFieldsWithFlags(id.toResourceId(), featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getProblemsIncludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsIncludingSubFieldsWithoutFeaturedFlag(id.toResourceId(), unlisted, pageable)
            .map { it.toResource() }

    override fun getContributorIdsExcludingSubFields(id: ThingId, pageable: Pageable): Page<ContributorId> =
        neo4jRepository.getContributorIdsExcludingSubFields(id.toResourceId(), pageable)

    override fun getPapersExcludingSubFields(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getPapersExcludingSubFields(id.toResourceId(), pageable).map { it.toResource() }

    override fun getPapersExcludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersExcludingSubFieldsWithFlags(id.toResourceId(), featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getPapersExcludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getPapersExcludingSubFieldsWithoutFeaturedFlag(id.toResourceId(), unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getComparisonsExcludingSubFields(id.toResourceId(), pageable)
            .map { it.toResource() }

    override fun getComparisonsExcludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsExcludingSubFieldsWithFlags(id.toResourceId(), featured, unlisted, pageable)
            .map { it.toResource() }

    override fun getComparisonsExcludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getComparisonsExcludingSubFieldsWithoutFeaturedFlag(id.toResourceId(), unlisted, pageable)
            .map { it.toResource() }

    override fun getProblemsExcludingSubFields(id: ThingId, pageable: Pageable): Page<Resource> =
        neo4jRepository.getProblemsExcludingSubFields(id.toResourceId(), pageable).map { it.toResource() }

    override fun getProblemsExcludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsExcludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getProblemsExcludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getProblemsExcludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun findResearchFieldsWithBenchmarks(): Iterable<Resource> =
        neo4jRepository.findResearchFieldsWithBenchmarks().map { it.toResource() }

    override fun getVisualizationsIncludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsIncludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsIncludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsIncludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsIncludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsIncludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListIncludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListIncludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListIncludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getVisualizationsExcludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsExcludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getVisualizationsExcludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsExcludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsExcludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getSmartReviewsExcludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListExcludingSubFieldsWithFlags(
        id: ThingId,
        featured: Boolean,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListExcludingSubFieldsWithFlags(
            id.toResourceId(),
            featured,
            unlisted,
            pageable
        ).map { it.toResource() }

    override fun getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(
        id: ThingId,
        unlisted: Boolean,
        pageable: Pageable
    ): Page<Resource> =
        neo4jRepository.getLiteratureListExcludingSubFieldsWithoutFeaturedFlag(
            id.toResourceId(),
            unlisted,
            pageable
        ).map { it.toResource() }

    private fun Neo4jProblemsPerField.toProblemsPerField() =
        ProblemsPerField(
            problem = problem.toResource(),
            papers = papers
        )
}
