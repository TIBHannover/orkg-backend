package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.api.ComparisonUseCases
import eu.tib.orkg.prototype.contenttypes.api.Identifiers
import eu.tib.orkg.prototype.contenttypes.application.ComparisonNotFound
import eu.tib.orkg.prototype.contenttypes.application.ComparisonRelatedFigureNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Comparison
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedFigure
import eu.tib.orkg.prototype.contenttypes.domain.model.ComparisonRelatedResource
import eu.tib.orkg.prototype.contenttypes.domain.model.PublicationInfo
import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.ContributionInfo
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.Thing
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ContributionComparisonRepository
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ComparisonService(
    private val repository: ContributionComparisonRepository,
    private val resourceRepository: ResourceRepository,
    private val statementRepository: StatementRepository
) : ComparisonUseCases {
    override fun findById(id: ThingId): Optional<Comparison> =
        resourceRepository.findById(id)
            .filter { it is Resource && Classes.comparison in it.classes }
            .map { it.toComparison() }

    override fun findAll(pageable: Pageable): Page<Comparison> =
        resourceRepository.findAllByClass(Classes.comparison, pageable)
            .pmap { it.toComparison() }

    override fun findAllByDOI(doi: String, pageable: Pageable): Page<Comparison> =
        statementRepository.findAllBySubjectClassAndDOI(Classes.comparison, doi, pageable)
            .pmap { it.toComparison() }

    override fun findAllByTitle(title: String, pageable: Pageable): Page<Comparison> =
        resourceRepository.findAllByClassAndLabel(Classes.comparison, SearchString.of(title, exactMatch = true), pageable)
            .pmap { it.toComparison() }

    override fun findAllByVisibility(visibility: VisibilityFilter, pageable: Pageable): Page<Comparison> =
        when (visibility) {
            VisibilityFilter.ALL_LISTED -> resourceRepository.findAllListedByClass(Classes.comparison, pageable)
            VisibilityFilter.NON_FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.comparison, Visibility.DEFAULT, pageable)
            VisibilityFilter.UNLISTED -> resourceRepository.findAllByClassAndVisibility(Classes.comparison, Visibility.UNLISTED, pageable)
            VisibilityFilter.FEATURED -> resourceRepository.findAllByClassAndVisibility(Classes.comparison, Visibility.FEATURED, pageable)
            VisibilityFilter.DELETED -> resourceRepository.findAllByClassAndVisibility(Classes.comparison, Visibility.DELETED, pageable)
        }.pmap { it.toComparison() }

    override fun findRelatedResourceById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedResource> =
        statementRepository.findBySubjectIdAndPredicateIdAndObjectId(comparisonId, Predicates.hasRelatedFigure, id)
            .filter { it.`object` is Resource && Classes.comparisonRelatedResource in it.`object`.classes }
            .map { it.`object`.toComparisonRelatedResource() }

    override fun findAllRelatedResources(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedResource> =
        statementRepository.findAllBySubjectAndPredicate(comparisonId, Predicates.hasRelatedFigure, pageable)
            .map { it.`object`.toComparisonRelatedResource() }

    override fun findRelatedFigureById(comparisonId: ThingId, id: ThingId): Optional<ComparisonRelatedFigure> =
        statementRepository.findBySubjectIdAndPredicateIdAndObjectId(comparisonId, Predicates.hasRelatedFigure, id)
            .filter { it.`object` is Resource && Classes.comparisonRelatedFigure in it.`object`.classes }
            .map { it.`object`.toComparisonRelatedFigure() }

    override fun findAllRelatedFigures(comparisonId: ThingId, pageable: Pageable): Page<ComparisonRelatedFigure> =
        statementRepository.findAllBySubjectAndPredicate(comparisonId, Predicates.hasRelatedFigure, pageable)
            .map { it.`object`.toComparisonRelatedFigure() }

    override fun findAllByContributor(contributorId: ContributorId, pageable: Pageable): Page<Comparison> =
        resourceRepository.findAllByClassAndCreatedBy(Classes.comparison, contributorId, pageable)
            .pmap { it.toComparison() }

    override fun findContributionsDetailsById(ids: List<ThingId>, pageable: Pageable): Page<ContributionInfo> =
        repository.findContributionsDetailsById(ids, pageable)

    private fun Thing.toComparisonRelatedResource(): ComparisonRelatedResource {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedResource(
            id = this@toComparisonRelatedResource.id,
            label = this@toComparisonRelatedResource.label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            url = statements.wherePredicate(Predicates.hasURL).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel()
        )
    }
    
    private fun Thing.toComparisonRelatedFigure(): ComparisonRelatedFigure {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return ComparisonRelatedFigure(
            id = this@toComparisonRelatedFigure.id,
            label = this@toComparisonRelatedFigure.label,
            image = statements.wherePredicate(Predicates.hasImage).firstObjectLabel(),
            description = statements.wherePredicate(Predicates.description).firstObjectLabel()
        )
    }

    private fun Resource.toComparison(): Comparison {
        val statements = statementRepository.findAllBySubject(id, PageRequests.ALL)
            .content
            .withoutObjectsWithBlankLabels()
        return Comparison(
            id = id,
            title = label,
            description = statements.wherePredicate(Predicates.description).firstObjectLabel(),
            researchFields = statements.wherePredicate(Predicates.hasSubject).objectIdsAndLabel(),
            identifiers = statements.associateIdentifiers(Identifiers.comparison),
            publicationInfo = PublicationInfo.from(statements),
            authors = statements.authors(statementRepository),
            contributions = statements.wherePredicate(Predicates.comparesContribution).objectIdsAndLabel(),
            visualizations = statements.wherePredicate(Predicates.hasVisualization).objectIdsAndLabel(),
            relatedFigures = statements.wherePredicate(Predicates.hasRelatedFigure).objectIdsAndLabel(),
            relatedResources = statements.wherePredicate(Predicates.hasRelatedResource).objectIdsAndLabel(),
            references = statements.wherePredicate(Predicates.reference)
                .withoutObjectsWithBlankLabels()
                .objects()
                .filterIsInstance<Literal>()
                .map { it.label },
            observatories = listOf(observatoryId),
            organizations = listOf(organizationId),
            extractionMethod = extractionMethod,
            createdAt = createdAt,
            createdBy = createdBy,
            previousVersion = statements.wherePredicate(Predicates.hasPreviousVersion).firstObjectId(),
            isAnonymized = statements.wherePredicate(Predicates.isAnonymized)
                .firstOrNull { it.`object` is Literal && it.`object`.datatype == Literals.XSD.BOOLEAN.prefixedUri }
                ?.`object`?.label.toBoolean(),
            visibility = visibility
        )
    }
}
