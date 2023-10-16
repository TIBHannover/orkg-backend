package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ContributorId
import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.statements.api.VisibilityFilter
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import java.time.OffsetDateTime
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResourceRepository : EntityRepository<Resource, ThingId> {
    fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource?

    // legacy methods:
    fun nextIdentity(): ThingId
    fun save(resource: Resource)
    fun deleteById(id: ThingId)
    fun deleteAll()
    fun findById(id: ThingId): Optional<Resource>
    fun findAllByLabel(labelSearchString: SearchString, pageable: Pageable): Page<Resource>
    fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndCreatedBy(`class`: ThingId, createdBy: ContributorId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabel(`class`: ThingId, labelSearchString: SearchString, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelAndCreatedBy(
        `class`: ThingId,
        labelSearchString: SearchString,
        createdBy: ContributorId,
        pageable: Pageable
    ): Page<Resource>
    fun findAllIncludingAndExcludingClasses(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        pageable: Pageable
    ): Page<Resource>
    fun findAllIncludingAndExcludingClassesByLabel(
        includeClasses: Set<ThingId>,
        excludeClasses: Set<ThingId>,
        labelSearchString: SearchString,
        pageable: Pageable
    ): Page<Resource>
    fun findPaperByLabel(label: String): Optional<Resource>
    fun findAllPapersByLabel(label: String): Iterable<Resource>
    fun findAllByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findPaperById(id: ThingId): Optional<Resource>
    fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource>
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
    fun findAllComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>

    fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListed(pageable: Pageable): Page<Resource>

    fun findAllByClassAndVisibility(classId: ThingId, visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedByClass(classId: ThingId, pageable: Pageable): Page<Resource>

    fun findAllByClassInAndVisibility(classes: Set<ThingId>, visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource>

    fun findAllByClassInAndVisibilityAndObservatoryId(classes: Set<ThingId>, visibility: Visibility, id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllListedByClassInAndObservatoryId(classes: Set<ThingId>, id: ObservatoryId, pageable: Pageable): Page<Resource>

    fun findAllWithFilters(
        classes: Set<ThingId> = emptySet(),
        visibility: VisibilityFilter? = null,
        organizationId: OrganizationId? = null,
        observatoryId: ObservatoryId? = null,
        createdBy: ContributorId? = null,
        createdAt: OffsetDateTime? = null,
        pageable: Pageable
    ): Page<Resource>
}
