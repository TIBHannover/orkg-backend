package eu.tib.orkg.prototype.statements.spi

import eu.tib.orkg.prototype.community.domain.model.ObservatoryId
import eu.tib.orkg.prototype.community.domain.model.OrganizationId
import eu.tib.orkg.prototype.contributions.domain.model.ContributorId
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
    fun findByLabel(label: String?): Optional<Resource>
    fun findAllByLabel(label: String): Iterable<Resource>
    fun findByClassAndObservatoryId(`class`: ThingId, id: ObservatoryId): Iterable<Resource>
    fun findAllByVerifiedIsTrue(pageable: Pageable): Page<Resource>
    fun findAllByVerifiedIsFalse(pageable: Pageable): Page<Resource>
    fun findAllByFeaturedIsTrue(pageable: Pageable): Page<Resource>
    fun findAllByFeaturedIsFalse(pageable: Pageable): Page<Resource>
    fun findAllByUnlistedIsTrue(pageable: Pageable): Page<Resource>
    fun findAllByUnlistedIsFalse(pageable: Pageable): Page<Resource>
    fun findPaperByResourceId(id: ThingId): Optional<Resource>
    fun findAllVerifiedPapers(pageable: Pageable): Page<Resource>
    fun findAllUnverifiedPapers(pageable: Pageable): Page<Resource>
    fun findAllFeaturedPapers(pageable: Pageable): Page<Resource>
    fun findAllNonFeaturedPapers(pageable: Pageable): Page<Resource>
    fun findAllUnlistedPapers(pageable: Pageable): Page<Resource>
    fun findAllListedPapers(pageable: Pageable): Page<Resource>
    fun findAllFeaturedResourcesByClass(classes: List<ThingId>, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllFeaturedResourcesByClass(classes: List<ThingId>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllFeaturedResourcesByObservatoryIDAndClass(id: ObservatoryId, classes: List<ThingId>, featured: Boolean, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllResourcesByObservatoryIDAndClass(id: ObservatoryId, classes: List<ThingId>, unlisted: Boolean, pageable: Pageable): Page<Resource>
    fun findAllContributorIds(pageable: Pageable): Page<ContributorId>
    fun findComparisonsByOrganizationId(id: OrganizationId, pageable: Pageable): Page<Resource>
}
