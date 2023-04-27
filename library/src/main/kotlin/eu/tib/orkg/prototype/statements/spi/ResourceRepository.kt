package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
import eu.tib.orkg.prototype.contenttypes.domain.model.Visibility
import eu.tib.orkg.prototype.statements.domain.model.Resource
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import java.util.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ResourceRepository : EntityRepository<Resource, ThingId> {
    fun findByIdAndClasses(id: ThingId, classes: Set<ThingId>): Resource?

    // legacy methods:
    fun nextIdentity(): ThingId
    fun save(resource: Resource)
    fun deleteByResourceId(id: ThingId)
    fun deleteAll()
    fun findByResourceId(id: ThingId): Optional<Resource>
    fun findAllByLabelMatchesRegex(label: String, pageable: Pageable): Page<Resource>
    fun findAllByLabelContaining(part: String, pageable: Pageable): Page<Resource>
    fun findAllByClass(`class`: ThingId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndCreatedBy(`class`: ThingId, createdBy: ContributorId, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabel(`class`: ThingId, label: String, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelAndCreatedBy(`class`: ThingId, label: String, createdBy: ContributorId, pageable: Pageable): Page<Resource>

    fun findAllByClassAndLabelMatchesRegex(`class`: ThingId, label: String, pageable: Pageable): Page<Resource>
    fun findAllByClassAndLabelMatchesRegexAndCreatedBy(`class`: ThingId, label: String, createdBy: ContributorId, pageable: Pageable): Page<Resource>

    fun findAllIncludingAndExcludingClasses(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>, pageable: Pageable): Page<Resource>
    fun findAllIncludingAndExcludingClassesByLabel(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>, label: String, pageable: Pageable): Page<Resource>
    fun findAllIncludingAndExcludingClassesByLabelMatchesRegex(includeClasses: Set<ThingId>, excludeClasses: Set<ThingId>, label: String, pageable: Pageable): Page<Resource>
    fun findByLabel(label: String): Optional<Resource>
    fun findAllByLabel(label: String): Iterable<Resource>
    fun findByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId): Iterable<Resource>
    fun findPaperByResourceId(id: ThingId): Optional<Resource>
    fun findAllPapersByVerified(verified: Boolean, pageable: Pageable): Page<Resource>
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
    fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>

    fun findAllByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListed(pageable: Pageable): Page<Resource>

    fun findAllPapersByVisibility(visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedPapers(pageable: Pageable): Page<Resource>

    fun findAllByClassInAndVisibility(classes: Set<ThingId>, visibility: Visibility, pageable: Pageable): Page<Resource>
    fun findAllListedByClassIn(classes: Set<ThingId>, pageable: Pageable): Page<Resource>

    fun findAllByClassInAndVisibilityAndObservatoryId(classes: Set<ThingId>, visibility: Visibility, id: ObservatoryId, pageable: Pageable): Page<Resource>
    fun findAllListedByClassInAndObservatoryId(classes: Set<ThingId>, id: ObservatoryId, pageable: Pageable): Page<Resource>
}
